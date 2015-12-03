package com.noomtech.photoviewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;


/**
 *
 * */
class PhotoPanel extends JPanel {


    private Image photo;


    PhotoPanel(Image photo) {
        this.photo = photo;
        setLayout(new BorderLayout());
        add(new JLabel(new ImageIcon(photo)), BorderLayout.CENTER);
    }


    public void paint(Graphics g) {
        super.paint(g);
        double photoWidth = photo.getWidth(null);
        double photoHeight = photo.getHeight(null);
        boolean needToScale = false;

        if(photoWidth > getWidth()) {
            double ratio = photoHeight/photoWidth;
            photoWidth = getWidth();
            photoHeight = Math.round(photoWidth * ratio);
            needToScale = true;
        }

        if(photoHeight > getHeight()) {
            double ratio = photoWidth/photoHeight;
            photoHeight = getHeight();
            photoWidth = Math.round(photoHeight * ratio);;
            needToScale = true;
        }

        if(needToScale) {
            Color c = g.getColor();
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(photo, (int)Math.round(getWidth() / 2 - (photoWidth / 2)), (int)Math.round(getHeight() / 2 - (photoHeight / 2)),
                    (int) photoWidth, (int) photoHeight, null);
            g.setColor(c);
        }
    }
}
