package com.noomtech.photoviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;


/**
 *
 */
public class CommandFrame extends JFrame {


    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String COMMA = ",";
    private JFrame photoFrame;
    private JFrame aliasFrame;
    private JTextArea shell;

    private File[] photos;
    private int currentPhotoIndex = 0;
    private Map<Integer,List<File>> requestedFileStructure = new HashMap<>();
    private HashSet<String> aliasResolver = new HashSet<>();
    private String destinationDirectoryString;
    private int currentAliasNumber = 0;
    private List<File> forDeletion = new ArrayList<File>();
    private List<File> cannotHandle = new ArrayList();


    public static void main(String[] args) {
        if(args == null || args.length < 1) {
            throw new IllegalArgumentException("Please provide a destination directory!");
        }

        if(!new File(args[0]).exists()) {
            throw new IllegalArgumentException("Destination directory of " + args[0] + " must exist!");
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(null, "Process Photos From Directory");

        File selectedFile = fileChooser.getSelectedFile();
        //¬File selectedFile = new File("C:/temp/testPhotos");
        if(selectedFile != null) {
            new CommandFrame(selectedFile.getAbsolutePath(), args[0]);
        }
    }

    public CommandFrame(String picturesDirectory, String destinationDirectory) {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        File[] files = new File(picturesDirectory).listFiles();
        File[] canKeep = new File[files.length];
        int numberOfPhotosToKeep = 0;
        for(File f : files) {
            String name = f.getName();
            if(f.isFile()) {
                if(!(name.endsWith(".mov") || name.endsWith(".MOV"))) {
                    canKeep[numberOfPhotosToKeep] = f;
                    numberOfPhotosToKeep++;
                }
                else {
                    cannotHandle.add(f);
                }
            }
        }

        if(numberOfPhotosToKeep > 0) {

            photos = new File[numberOfPhotosToKeep];
            System.arraycopy(canKeep, 0, photos, 0, numberOfPhotosToKeep);

            getContentPane().setLayout(new BorderLayout());

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int heightUnit = screenSize.height / 50;
            int widthUnit = screenSize.width / 50;

            this.setMinimumSize(new Dimension(widthUnit * 50, heightUnit * 6));
            this.setPreferredSize(new Dimension(widthUnit * 50, heightUnit * 6));
            setLocation(new Point(0, 0));
            shell = new JTextArea();
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.getViewport().add(shell);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            shell.addKeyListener(new CommandListener(shell));
            shell.setBorder(BorderFactory.createTitledBorder("Enter Command"));

            photoFrame = new JFrame();
            photoFrame.setMinimumSize(new Dimension(widthUnit * 50, heightUnit * 35));
            photoFrame.setPreferredSize(new Dimension(widthUnit * 50, heightUnit * 35));
            photoFrame.setLocation(new Point(0, heightUnit * 6));
            photoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            aliasFrame = new AliasFrame(Collections.EMPTY_MAP);
            aliasFrame.setMinimumSize(new Dimension(widthUnit * 50, heightUnit * 9));
            aliasFrame.setPreferredSize(new Dimension(widthUnit * 50, heightUnit * 9));
            aliasFrame.setLocation(0, heightUnit * 41);
            aliasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            aliasFrame.setVisible(true);
            this.setVisible(true);
            photoFrame.setVisible(true);

            destinationDirectoryString = destinationDirectory + FILE_SEPARATOR;

            setTitle("f=forward,b=back,p=process,l=do nothing,d=del,d'alias'=alias + del afterwards,q=quit,alias,month,year=create alias with month and year | alias=create alias");
            pack();

            updatePhoto(getCurrentPhoto());

            shell.requestFocus();
            shell.setCaretPosition(0);
        }
        else {
            JOptionPane.showMessageDialog(this, "No valid photos in directory " + picturesDirectory, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void updatePhoto(File photo) {
        Point location = photoFrame.getLocation();
        Dimension size = photoFrame.getMinimumSize();

        photoFrame.setVisible(false);
        photoFrame.dispose();

        photoFrame = new JFrame();
        photoFrame.setLocation(location);
        photoFrame.setMinimumSize(size);
        photoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        photoFrame.add(new PhotoPanel(photo));

        pack();

        photoFrame.setVisible(true);
    }

    private class CommandListener implements KeyListener {

        private JTextArea textArea;
        private StringBuilder newText = new StringBuilder();

        private CommandListener(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                String command = newText.toString();
                newText = new StringBuilder();

                String outputString = null;
                switch (command) {
                    case "f": {
                        //skip
                        outputString = "Skipped " + getCurrentPhoto().getName();
                        moveToNextPhoto();
                        break;
                    }
                    case "b": {
                        //back
                        outputString = "Skipped " + getCurrentPhoto().getName();
                        moveToPreviousPhoto();
                        break;
                    }
                    case "filter": {

                    }
                    case "d": {
                        //delete
                        forDeletion.add(getCurrentPhoto());
                        boolean noMorePhotos = removeCurrentPhotoFromList();
                        if(noMorePhotos) {
                            saveOrQuit();
                        }

                        outputString = "Marked " + getCurrentPhoto().getName() + " for deletion";
                        break;
                    }
                    case "l": {
                        //leave it alone
                        boolean noMorePhotos = removeCurrentPhotoFromList();
                        if(noMorePhotos) {
                            saveOrQuit();
                        }

                        outputString = "Did nothing with " + getCurrentPhoto().getName();
                        break;
                    }
                    case "q": {
                        //quit
                        photoFrame.dispose();
                        aliasFrame.dispose();
                        dispose();
                        System.exit(0);
                    }
                    case "p": {
                        process();
                        outputString = "Processed";
                        break;
                    }
                    default: {

                        boolean deleteAfterwards = false;
                        String aliasString = command;
                        if(command.matches("d[0-9][0-9]*[0-9]*")) {
                            aliasString = command.substring(1, command.length());
                            deleteAfterwards = true;
                        }

                        File currentPhoto = getCurrentPhoto();
                        int alias = -1;
                        try {
                            alias = Integer.parseInt(aliasString);
                        }
                        catch(NumberFormatException n) {}

                        if(alias > -1) {
                            List<File> fileList = requestedFileStructure.get(alias);
                            if(fileList == null) {
                                outputString = "Unknown Alias";
                            }
                            else {
                                fileList.add(currentPhoto);
                                boolean noMorePhotos = removeCurrentPhotoFromList();
                                if(deleteAfterwards) {
                                    forDeletion.add(currentPhoto);
                                    outputString = "Added " + getCurrentPhoto().getName() + " to alias " + command + " and marked it for deletion";
                                }
                                else {
                                    outputString = "Added " + getCurrentPhoto().getName() + " to alias " + command;
                                }

                                if(noMorePhotos) {
                                    saveOrQuit();
                                }
                            }
                        }
                        else if(command.contains(COMMA) && !command.equals("") && !command.contains("/n") && !command.contains("/t") && !command.startsWith(" ")) {
                            if (command.endsWith(" ")) {
                                while (command.endsWith(" ")) {
                                    command = command.substring(0, command.length() - 1);
                                }
                            }

                            String[] components = command.split(COMMA);
                            String newAliasString = null;
                            if (components.length != 3) {
                                outputString = "Invalid alias format.  Should be 'alias name','month','year'";
                            } else {
                                newAliasString = components[2] + " " + components[1] + " " + components[0];
                            }

                            if (outputString == null) {
                                if (aliasResolver.contains(newAliasString)) {
                                    outputString = newAliasString + " is already aliased";
                                } else {
                                    String dirName = destinationDirectoryString + newAliasString;
                                    List<File> fileList = new ArrayList<>(1);
                                    fileList.add(new File(dirName));
                                    fileList.add(getCurrentPhoto());
                                    currentAliasNumber++;

                                    requestedFileStructure.put(currentAliasNumber, fileList);
                                    aliasResolver.add(newAliasString);

                                    Point location = aliasFrame.getLocation();
                                    Dimension size = aliasFrame.getPreferredSize();
                                    aliasFrame.setVisible(false);
                                    aliasFrame.dispose();
                                    aliasFrame = new AliasFrame(requestedFileStructure);
                                    aliasFrame.setMinimumSize(size);
                                    aliasFrame.setPreferredSize(size);
                                    aliasFrame.setLocation(location);
                                    aliasFrame.pack();
                                    aliasFrame.setVisible(true);

                                    outputString = "Alias " + currentAliasNumber + "='" + newAliasString + "' created";
                                }
                            }
                        }
                        else {
                            outputString = "Invalid Command";
                        }
                    }
                }

                textArea.setText("");
                textArea.setCaretPosition(0);
                textArea.requestFocus();
                shell.setBorder(BorderFactory.createTitledBorder(outputString));

            }
            else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE && newText.length() > 0) {
                newText = new StringBuilder(newText.subSequence(0,newText.length() - 1));
            }
            else {
                newText.append(e.getKeyChar());
            }

        }
    }

//    public static void main(String[] args) throws InterruptedException {
//        PhotoPanel p = new PhotoPanel("C:/temp/leaves.png");
//        Thread.sleep(3000);
//        p.setPhoto("C:/temp/sky.png");
//    }

    private void process() {

        if(requestedFileStructure.isEmpty() && forDeletion.isEmpty() && photos.length > 0) {
            JOptionPane.showMessageDialog(getContentPane(), "Nothing to process.  Please categorise some photos", "",
                    JOptionPane.WARNING_MESSAGE);
        }
        else {
            for (Map.Entry<Integer, List<File>> entry : requestedFileStructure.entrySet()) {
                List<File> fileList = entry.getValue();
                File destinationDirectory = fileList.get(0);
                if (!destinationDirectory.exists()) {
                    destinationDirectory.mkdir();
                }

                try {
                    for (int i = 1; i < fileList.size(); i++) {
                        File sourceFile = fileList.get(i);
                        copyFile(sourceFile, new File(destinationDirectory.getAbsolutePath() + File.separator + sourceFile.getName()));
                }
                } catch (IOException e) {
                    String message = "Could not copy files to destination: " + e.getMessage();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(getContentPane(), message, "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }

            for(File toDelete : forDeletion) {
                try {
                    if(!toDelete.delete()) {
                        throw new IllegalStateException("Could not delete file " + toDelete.getAbsolutePath());
                    }
                }
                catch(Exception e) {
                    JOptionPane.showMessageDialog(null, "Could not delete file " + toDelete.getAbsolutePath() + ": " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            for(File cannotHandleThisFile : cannotHandle) {
                File cannotHandleDestinationDirectory = new File(destinationDirectoryString + File.separator + "cannotHandle");
                if(!cannotHandleDestinationDirectory.exists()) {
                    cannotHandleDestinationDirectory.mkdir();
                }

                try {
                    copyFile(cannotHandleThisFile, new File(cannotHandleDestinationDirectory + File.separator + cannotHandleThisFile.getName()));
                }
                catch(IOException e) {
                    JOptionPane.showMessageDialog(null, "Could copy non-picture file across " +
                                    cannotHandleThisFile.getAbsolutePath() + ": " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            Iterator<Integer> iter = requestedFileStructure.keySet().iterator();
            while(iter.hasNext()) {
                List<File> fileList = requestedFileStructure.get(iter.next());
                if(fileList.size() > 1) {
                    for(int i = fileList.size() - 1 ; i >= 1; i--) {
                        fileList.remove(i);
                    }
                }
            }

            forDeletion.clear();
            cannotHandle.clear();
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        try(FileInputStream reader = new FileInputStream(src);
            FileOutputStream writer = new FileOutputStream(dest)) {

            int i;
            byte[] b = new byte[1024];
            while((i = reader.read(b)) != -1) {
                writer.write(b,0,i);
            }
        }
    }

    private void setDisplayedPhoto(final File file) {
        updatePhoto(file);
    }

    private void moveToNextPhoto() {
        if(currentPhotoIndex == photos.length - 1) {
            currentPhotoIndex = 0;
        }
        else {
            currentPhotoIndex++;
        }

        setDisplayedPhoto(getCurrentPhoto());
    }

    private void moveToPreviousPhoto() {
        if(currentPhotoIndex == 0) {
            currentPhotoIndex = photos.length - 1;
        }
        else {
            currentPhotoIndex--;
        }

        setDisplayedPhoto(getCurrentPhoto());
    }

    private File getCurrentPhoto() {
        return photos[currentPhotoIndex];
    }

    private boolean removeCurrentPhotoFromList() {
        if(photos.length == 1) {
            return true;
        }

        File[] newPhotos = new File[photos.length - 1];
        int indexInNewPhotos = 0;
        int indexInOldPhotos = 0;
        while(indexInNewPhotos < newPhotos.length) {

            if(indexInOldPhotos != currentPhotoIndex) {
                newPhotos[indexInNewPhotos] = photos[indexInOldPhotos];
                indexInNewPhotos++;
            }

            indexInOldPhotos++;
        }

        photos = newPhotos;
        if(currentPhotoIndex > (photos.length - 1)) {
            currentPhotoIndex = 0;
        }
        setDisplayedPhoto(getCurrentPhoto());
        return false;
    }

    private void saveOrQuit() {
        int choice = JOptionPane.showConfirmDialog(getContentPane(), "No more photos left to categorise.  " +
                "Would you like to save?");
        if (choice == JOptionPane.OK_OPTION) {
            process();
        }

        System.exit(0);
    }
}