package com.noomtech.photoviewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.media.*;


/**
 *
 * */
class PhotoPanel extends JPanel {


    private Image photo;


    PhotoPanel(File photoFile) {

        setLayout(new BorderLayout());

        Component componentToAdd;
        try {
            String fileName = photoFile.getName();
            if(!fileName.endsWith(".mov") && !fileName.endsWith(".MOV")) {
                ImageIcon icon = new ImageIcon(photoFile.getAbsolutePath());
                photo = icon.getImage();
                componentToAdd = new JLabel(new ImageIcon(photo));
            }
            else {
                Player player = Manager.createRealizedPlayer(photoFile.toURI().toURL());
                componentToAdd = player.getVisualComponent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JLabel label = new JLabel("Could not display " + photoFile.getName());
            Font f = label.getFont();
            label.setFont(f.deriveFont(f.getSize() + 5));
            componentToAdd = label;
        }

        add(componentToAdd, BorderLayout.CENTER);
    }


    public void paint(Graphics g) {
        super.paint(g);
        if(photo != null) {
            double photoWidth = photo.getWidth(null);
            double photoHeight = photo.getHeight(null);
            boolean needToScale = false;

            if (photoWidth > getWidth()) {
                double ratio = photoHeight / photoWidth;
                photoWidth = getWidth();
                photoHeight = Math.round(photoWidth * ratio);
                needToScale = true;
            }

            if (photoHeight > getHeight()) {
                double ratio = photoWidth / photoHeight;
                photoHeight = getHeight();
                photoWidth = Math.round(photoHeight * ratio);
                ;
                needToScale = true;
            }

            if (needToScale) {
                Color c = g.getColor();
                g.setColor(Color.white);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.drawImage(photo, (int) Math.round(getWidth() / 2 - (photoWidth / 2)), (int) Math.round(getHeight() / 2 - (photoHeight / 2)),
                        (int) photoWidth, (int) photoHeight, null);
                g.setColor(c);
            }
        }
    }
}
