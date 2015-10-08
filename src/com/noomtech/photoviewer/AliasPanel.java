package com.noomtech.photoviewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
class AliasPanel extends JPanel {


    private JLabel aliasLabel;
    private static String EQUALS = "=";
    private static String NEWLINE = System.getProperty("line.separator");


    AliasPanel() {
        setLayout(new BorderLayout());
    }


    void setAliases(final Map<Integer,List<File>> aliasStructure) {

        if (aliasLabel != null) {
            remove(aliasLabel);
        }

        StringBuilder sb = new StringBuilder();
        List<Integer> keyList = new ArrayList<>(aliasStructure.size());
        for (Integer i : aliasStructure.keySet()) {
            keyList.add(i);
        }

        Collections.sort(keyList);

        for (int i = 0; i < keyList.size(); i++) {
            Integer key = keyList.get(i);
            sb.append(key + EQUALS + aliasStructure.get(key).get(0) + NEWLINE);
        }

        aliasLabel = new JLabel(sb.toString());

        add(aliasLabel, BorderLayout.CENTER);

        doLayout();
    }
}
