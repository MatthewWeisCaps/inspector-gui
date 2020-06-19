/*
 * Copyright (c) 2020, Matthew Weis, Kansas State University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sireum.hamr.inspector.gui.components;

import art.DataContent;
import javafx.scene.control.TableCell;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.sireum.hamr.inspector.common.Msg;

/**
 * A cell which visualizes {@link art.DataContent} with matching paren highlighting.
 */
@Slf4j
public class DataContentTableCell extends TableCell<Msg, DataContent> {

    @Override
    protected void updateItem(DataContent item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            setGraphic(createTextFlow(item.toString()));
        }
    }

    private TextFlow createTextFlow(String dataContent) {
        final TextFlow textFlow = TextFlowFactory.createDefaultParenMatchingTextFlow(dataContent);

        // todo see if there is a good way to reduce the large number of listeners this creates
        // make sure height is calculated from table column width
        // https://stackoverflow.com/questions/42855724/textflow-inside-tablecell-not-correct-cell-height
        final int padding = 4;
        setPrefHeight(textFlow.prefHeight(getTableColumn().getWidth()) + padding);
        getTableColumn().widthProperty().addListener((observable, oldValue, newValue) -> {
            setPrefHeight(textFlow.prefHeight(getTableColumn().getWidth()) + padding);
        });

        return textFlow;
    }
}
