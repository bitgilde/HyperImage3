/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperimage.client.gui.lists;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;

/**
 *
 * @author Jens-Martin Loebel <loebel@bitgilde.de>
 */
public class TagListCellRenderer extends JPanel implements ListCellRenderer {
    private static final long serialVersionUID = 1L;
    
    private JLabel cellLabel = new JLabel();
    private ImageIcon tagIcon = new ImageIcon(getClass().getResource("/resources/icons/tag-icon.png"));


    public TagListCellRenderer() {
        cellLabel.setFont(cellLabel.getFont().deriveFont((float) 12));

        this.setLayout(new BorderLayout());
        this.add(cellLabel, BorderLayout.CENTER);
    }

    public static String getTagName(HiGroup tag) {
        HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(tag, HIRuntime.getManager().getProject().getDefaultLanguage());
        if ( record != null ) {
            String title = MetadataHelper.findValue("HIBase", "title", record);
            if ( title.length() > 0 ) return title;
        }
        
        return "Tag ("+tag.getUUID().split("-")[4]+")";
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        HiGroup tag;
        String tagName = value.toString();
        if ( value.getClass() == HiGroup.class ) {
            tag = (HiGroup) value;
            tagName = getTagName(tag);
            cellLabel.setIcon(tagIcon);
        } else cellLabel.setIcon(null);

        cellLabel.setText(tagName);

        if (isSelected) {
            this.setBackground(list.getSelectionBackground());
            cellLabel.setBackground(list.getSelectionBackground());
            cellLabel.setForeground(list.getSelectionForeground());
        } else {
            this.setBackground(list.getBackground());
            cellLabel.setBackground(list.getBackground());
            cellLabel.setForeground(list.getForeground());
        }
        this.setEnabled(list.isEnabled());
        cellLabel.setEnabled(list.isEnabled());
        cellLabel.setOpaque(true);
        this.setOpaque(true);


        return this;
    }
}
