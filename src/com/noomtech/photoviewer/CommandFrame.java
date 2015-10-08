package com.noomtech.photoviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;

/**
 *
 */
public class CommandFrame extends JFrame {


    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String UNDERSCORE = "_";
    private static final String UNKNOWN_YEAR = "UNKNOWN_YEAR";
    private static final String TWENTY = "20";
    private static final String[] MONTH_NAMES =
            {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October",
                    "November", "December"};
    private PhotoPanel photoPanel;
    private AliasPanel aliasPanel;

    private File[] photos;
    private int currentPhotoIndex = 0;
    private Map<Integer,List<File>> requestedFileStructure = new HashMap<>();
    private HashSet<String> aliasResolver = new HashSet<>();
    private String destinationDirectoryString;
    private int currentAliasNumber = 0;


    public static void main(String[] args) {
        if(args == null || args.length < 1) {
            throw new IllegalArgumentException("Please provide a destination directory!");
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setVisible(true);

        new CommandFrame(fileChooser.getSelectedFile().getAbsolutePath(), args[1]);
    }

    public CommandFrame(String picturesDirectory, String destinationDirectory) {

        photos = new File(picturesDirectory).listFiles();
        if(photos.length > 0) {

            getContentPane().setLayout(new GridBagLayout());
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            getContentPane().setPreferredSize(new Dimension(1300, 1000));
            setLocation(new Point((int) ((screenSize.getWidth() - 1300) / 2), (int) ((screenSize.getHeight() - 1000) / 2)));

            photoPanel = new PhotoPanel();
            aliasPanel = new AliasPanel();
            JTextArea shell = new JTextArea();
            GridBagConstraints gbc = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
                    new Insets(0,0,0,0),0,0);
            getContentPane().add(shell, gbc);
            gbc.gridx++;
            gbc.gridheight = 2;
            getContentPane().add(photoPanel);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridheight = 1;
            getContentPane().add(aliasPanel, gbc);

            photoPanel.setPhoto(getCurrentPhoto());
            shell.addKeyListener(new CommandListener(shell));

            destinationDirectoryString = destinationDirectory + FILE_SEPARATOR;

            pack();
            doLayout();
        }
        else {
            JOptionPane.showMessageDialog(this, "No photos in directory " + picturesDirectory, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }


    private class CommandListener implements KeyListener {

        private JTextArea textArea;
        private StringBuilder newText = new StringBuilder();

        private CommandListener(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                String command = newText.toString();
                newText = new StringBuilder();

                String outputString = null;
                switch (command) {
                    case "f": {
                        //skip
                        outputString = "Skipped";
                        moveToNextPhoto();
                        break;
                    }
                    case "b": {
                        //back
                        outputString = "Moved Back";
                        moveToPreviousPhoto();
                        break;
                    }
                    case "d": {
                        //delete
                        File toDelete = getCurrentPhoto();
                        removeCurrentPhotoFromList();
                        toDelete.delete();
                        if(photos.length == 0) {
                            int choice = JOptionPane.showConfirmDialog(getContentPane(), "No more photos left to categorise.  " +
                                    "Would you like to save?");
                            if(choice == JOptionPane.OK_OPTION) {
                                finishUp();
                            }
                            else {
                                System.exit(0);
                            }
                        }

                        outputString = "Deleted";
                        break;
                    }
                    case "q": {
                        //quit
                        System.exit(0);
                    }
                    case "p": {
                        finishUp();
                        break;
                    }
                    default: {

                        File currentPhoto = getCurrentPhoto();
                        int alias = -1;
                        try {
                            alias = Integer.parseInt(command);
                        }
                        catch(NumberFormatException n) {}

                        if(alias > -1) {
                            List<File> fileList = requestedFileStructure.get(alias);
                            if(fileList == null) {
                                outputString = "Unknown Alias";
                            }
                            else {
                                fileList.add(currentPhoto);
                                removeCurrentPhotoFromList();
                            }
                        }
                        else if(!command.equals("") && !command.contains("/n") && !command.contains("/t") && !command.startsWith(" ")) {
                            if(command.endsWith(" ")) {
                                while(command.endsWith(" ")) {
                                    command = command.substring(0, command.length() - 1);
                                }
                            }

                            if(aliasResolver.contains(command)) {
                                outputString = command + " is already aliased";
                            }
                            else {
                                String[] monthAndYearOfCreation = getMonthAndYearOfCreation(currentPhoto);
                                String dirName = destinationDirectoryString + monthAndYearOfCreation[0] + UNDERSCORE +
                                        monthAndYearOfCreation[1] + UNDERSCORE + command;
                                List<File> fileList = new ArrayList<>(1);
                                fileList.add(new File(dirName));
                                fileList.add(getCurrentPhoto());
                                currentAliasNumber++;

                                requestedFileStructure.put(currentAliasNumber, fileList);
                                aliasResolver.add(command);
                                aliasPanel.setAliases(requestedFileStructure);

                                outputString = "Alias " + currentAliasNumber + "=" + command + " created and photo added";
                            }
                        }
                        else {
                            outputString = "Invalid Command";
                        }
                    }
                }

                if(outputString != null) {
                    textArea.append(outputString + NEWLINE);
                }

            } else {
                newText.append(e.getKeyChar());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    static String[] getMonthAndYearOfCreation(File file) {
        try {
            BasicFileAttributes b = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(b.creationTime().toMillis());
            String creationMonth = theMonthName(calendar.MONTH);
            String creationYear = Integer.toString(calendar.YEAR);
            if(creationYear.startsWith(TWENTY)) {
                creationYear = UNKNOWN_YEAR;
            }
            return new String[]{creationMonth, creationYear};
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Could not get creation date for photo " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        return null;
    }

    private static String theMonthName(int month) {
        return MONTH_NAMES[month];
    }



//    public static void main(String[] args) throws InterruptedException {
//        PhotoPanel p = new PhotoPanel("C:/temp/leaves.png");
//        Thread.sleep(3000);
//        p.setPhoto("C:/temp/sky.png");
//    }

    private void finishUp() {

        if(requestedFileStructure.isEmpty()) {
            JOptionPane.showMessageDialog(getContentPane(), "Nothing to process.  Please categorise some photos", "",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
        else {
            for (Map.Entry<Integer, List<File>> entry : requestedFileStructure.entrySet()) {
                List<File> fileList = entry.getValue();
                File destinationDirectory = fileList.get(0);
                if (!destinationDirectory.exists()) {
                    createIfNotExist(destinationDirectory);
                }

                try {
                    for (int i = 1; i < fileList.size(); i++) {
                        File sourceFile = fileList.get(i);
                        copyFile(sourceFile, new File(destinationDirectoryString + sourceFile.getName()));
                    }
                } catch (IOException e) {
                    String message = "Could not copy files to destination: " + e.getMessage();
                    JOptionPane.showMessageDialog(getContentPane(), message, "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        try(FileReader reader = new FileReader(src); FileWriter writer = new FileWriter(dest)) {
            int i;
            while((i = reader.read()) != -1) {
                writer.write(i);
            }
        }
    }

    private void createIfNotExist(File childFileThatDoesNotExist) {
        File parent = childFileThatDoesNotExist.getParentFile();
        if(!parent.exists()) {
            createIfNotExist(parent);
        }

        try {
            childFileThatDoesNotExist.createNewFile();
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(getContentPane(), "Could not create file : " +
                    childFileThatDoesNotExist.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setDisplayedPhoto(final File file) {
        photoPanel.setPhoto(file);
        String[] creationTimeInfo = getMonthAndYearOfCreation(file);
        setTitle(creationTimeInfo[0] + " " + creationTimeInfo[1]);
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

    private void removeCurrentPhotoFromList() {
        File[] newPhotos = new File[photos.length - 1];
        int indexInNewPhotos = 0;
        int indexInOldPhotos = 0;
        while(indexInNewPhotos < newPhotos.length) {
            if(indexInOldPhotos == currentPhotoIndex) {
                if(indexInOldPhotos == photos.length - 1) {
                    indexInOldPhotos = 0;
                }
                else {
                    indexInOldPhotos++;
                }
            }

            newPhotos[indexInNewPhotos] = photos[indexInOldPhotos];
            indexInNewPhotos++;
            indexInOldPhotos++;
        }

        photos = newPhotos;
        setDisplayedPhoto(getCurrentPhoto());
    }
}
