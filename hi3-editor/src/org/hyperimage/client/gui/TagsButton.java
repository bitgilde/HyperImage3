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
package org.hyperimage.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import org.hyperimage.client.HIRuntime;

/**
 *
 * @author Jens-Martin Loebel <loebel@bitgilde.de>
 */
public class TagsButton extends JButton {

    private static final long serialVersionUID = 1L;

    @Override
    public void setEnabled(boolean enabled) {
        if (HIRuntime.getGui().checkEditAbility(true)) {
            super.setEnabled(enabled);
        } else {
            super.setEnabled(false);
        }
        if ( this.isEnabled() ) setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        else setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }
    
    public void setCount(int count) {
        setText("Tags ("+count+")");
    }

    public TagsButton() {
        super();
        setActionCommand("editBaseTags");
        setText("Tags (xxx)");
        setHorizontalAlignment(SwingConstants.LEFT);
        setIcon(new ImageIcon(getClass().getResource("/resources/icons/tag.png"))); //$NON-NLS-1$
        setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/tag-active.png"))); //$NON-NLS-1$
        setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/tag-disabled.png"))); //$NON-NLS-1$
        setPreferredSize(new Dimension(90, 24));
        setEnabled(this.isEnabled());
    }

}
