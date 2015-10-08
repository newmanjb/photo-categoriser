package com.noomtech.photoviewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;


/**
 *
 * */
class PhotoPanel extends JPanel {


    private JLabel photoLabel;


    PhotoPanel() {
        setLayout(new BorderLayout());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(900, 800));
        setLocation(new Point((int)((screenSize.getWidth() - 900) / 2), (int)((screenSize.getHeight() - 800) / 2)));
    }


    void setPhoto(final File imageFile) {
        setVisible(false);
        if (photoLabel != null) {
            remove(photoLabel);
        }

        ImageIcon photoIcon = null;
        try {
            photoIcon = new ImageIcon(imageFile.getAbsolutePath());
        } catch (Exception e) {

        }

        if (photoIcon != null) {
            photoLabel = new JLabel(new ImageIcon(imageFile.getAbsolutePath()));
            add(photoLabel, BorderLayout.CENTER);
        } else {
            photoLabel = new JLabel("Could not display " + imageFile.getAbsolutePath());
            add(photoLabel, BorderLayout.CENTER);
        }

        doLayout();
    }
}
