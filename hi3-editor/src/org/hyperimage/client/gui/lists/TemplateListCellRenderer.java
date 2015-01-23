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

/*
 * Copyright 2014 Leuphana Universität Lüneburg
 * All rights reserved.  Use is subject to license terms.
 */

/*
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.client.gui.lists;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.hyperimage.client.Messages;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 * @author Jens-Martin Loebel
 */
public class TemplateListCellRenderer extends JPanel implements ListCellRenderer {

    private JLabel cellLabel = new JLabel();
    private ImageIcon lockedIcon = new ImageIcon(getClass().getResource("/resources/icons/lock-icon.png"));
    private ImageIcon editIcon = new ImageIcon(getClass().getResource("/resources/icons/edit-icon.png"));


    public TemplateListCellRenderer() {
        cellLabel.setFont(cellLabel.getFont().deriveFont((float) 12));

        this.setLayout(new BorderLayout());
        this.add(cellLabel, BorderLayout.CENTER);
    }

    public static String getTemplateName(HiFlexMetadataTemplate template) {
        if (template.getNamespacePrefix().compareTo("HIInternal") == 0)
           return Messages.getString("TemplateListCellRenderer.STAMMDATEN");
        else if (template.getNamespacePrefix().compareTo("dc") == 0)
            return Messages.getString("TemplateListCellRenderer.DUBLIN_CORE_LEGACY_SET");
        else if (template.getNamespacePrefix().compareTo("cdwalite") == 0)
            return Messages.getString("TemplateListCellRenderer.CDWA_LITE");
        else if (template.getNamespacePrefix().compareTo("vra4") == 0)
            return Messages.getString("TemplateListCellRenderer.VRA_CORE_4");
        else if (template.getNamespacePrefix().compareTo("vra4hdlbg") == 0)
            return Messages.getString("TemplateListCellRenderer.VRA_CORE_4_HDLBG");
        else if (template.getNamespacePrefix().compareTo("HIClassic") == 0)
            return Messages.getString("TemplateListCellRenderer.HYPERIMAGE_CLASSIC");
        else if (template.getNamespacePrefix().equalsIgnoreCase("custom"))
            return Messages.getString("TemplateListCellRenderer.CUSTOM_TEMPLATE_(BEARBEITBAR)");
        else
            return template.getNamespacePrefix();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        HiFlexMetadataTemplate template = null;
        template = (HiFlexMetadataTemplate) value;

        String templateName = getTemplateName(template);

        cellLabel.setText(templateName);

        cellLabel.setIcon(lockedIcon);
        if ( template.getNamespacePrefix().toLowerCase().compareTo("custom") == 0 )
            cellLabel.setIcon(editIcon);

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
