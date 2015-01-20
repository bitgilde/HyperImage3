/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/HYPERIMAGE.LICENSE
 * or http://www.sun.com/cddl/cddl.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/HYPERIMAGE.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2009 Humboldt-Universitaet zu Berlin
 * All rights reserved.  Use is subject to license terms.
 */
package org.hyperimage.client.gui.lists;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 * @author Jens-Martin Loebel
 */
public class TemplateFieldsListCellRenderer extends JPanel implements ListCellRenderer  {

    private JLabel cellLabel = new JLabel();
    private ImageIcon singleLineIcon = new ImageIcon(getClass().getResource("/resources/icons/singleLine-icon.png"));
    private ImageIcon multiLineIcon = new ImageIcon(getClass().getResource("/resources/icons/multiLine-icon.png"));
    private HiFlexMetadataTemplate template;
    private Locale osLang;


    public TemplateFieldsListCellRenderer(HiFlexMetadataTemplate template, Locale osLang) {
        this.template = template;
        this.osLang = osLang;

        cellLabel.setFont(cellLabel.getFont().deriveFont((float) 12));

        this.setLayout(new BorderLayout());
        this.add(cellLabel, BorderLayout.CENTER);
    }

    public void setScreenLanguage(Locale osLang) {
        if ( osLang == null ) return;
        this.osLang = osLang;
    }

    public void setReferenceTemplate(HiFlexMetadataTemplate template) {
        this.template = template;
    }


    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {

        HiFlexMetadataSet set = null;
        set = (HiFlexMetadataSet) value;

        String displayName = MetadataHelper.getTemplateKeyDisplayName(template, set, MetadataHelper.localeToLangID(osLang));

        if ( displayName == null || displayName.length() == 0 ) displayName = "-";

        cellLabel.setText("<html>"+displayName+" <i>("+set.getTagname()+")</i></html>");

        if ( set.isRichText() ) cellLabel.setIcon(multiLineIcon);
        else cellLabel.setIcon(singleLineIcon);

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
