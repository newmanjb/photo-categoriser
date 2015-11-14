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
class AliasFrame extends JFrame {


    private JTextArea aliasText;
    private static String EQUALS = "=";
    private static String NEWLINE = System.getProperty("line.separator");


    AliasFrame(Map<Integer,List<File>> aliasStructure) {
        setLayout(new BorderLayout());

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

        aliasText = new JTextArea(sb.toString());
        aliasText.setEditable(false);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(aliasText);
        add(scrollPane, BorderLayout.CENTER);

        pack();
    }
}
