/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.maddyhome.idea.vim.action.copy

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.newapi.ExecutionContext
import com.maddyhome.idea.vim.newapi.VimCaret
import com.maddyhome.idea.vim.newapi.VimEditor
import com.maddyhome.idea.vim.newapi.injector
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.command.SelectionType
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.group.visual.VimSelection
import com.maddyhome.idea.vim.handler.VisualOperatorActionHandler
import com.maddyhome.idea.vim.helper.enumSetOf
import com.maddyhome.idea.vim.ui.ClipboardHandler
import java.util.*

/**
 * @author vlan
 */
class YankVisualLinesAction : VisualOperatorActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.COPY

  // 修改前
  /*
  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_MOT_LINEWISE, CommandFlags.FLAG_EXIT_VISUAL)
  */
  // 修改后
  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_EXIT_VISUAL)


  override fun executeForAllCarets(
    editor: Editor,
    context: DataContext,
    cmd: Command,
    caretsAndSelections: Map<Caret, VimSelection>,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val selections = caretsAndSelections.values
    val starts: MutableList<Int> = ArrayList()
    val ends: MutableList<Int> = ArrayList()
    selections.forEach { selection: VimSelection ->
      val textRange = selection.toVimTextRange(false)
      textRange.startOffsets.forEach { e: Int -> starts.add(e) }
      textRange.endOffsets.forEach { e: Int -> ends.add(e) }
    }
    val vimSelection = selections.firstOrNull() ?: return false
    val startsArray = starts.toIntArray()
    val endsArray = ends.toIntArray()

    // 修改前
    /*
    val selection = if (vimSelection.type == SelectionType.BLOCK_WISE) SelectionType.BLOCK_WISE else SelectionType.LINE_WISE
    return injector.yank.yankRange(editor, TextRange(startsArray, endsArray), selection, true)
     */
    // 修改: [ADDED] visual Y 复制内容到系统粘贴板 #2. 通过 map vnoremap Y "*y", 还不支持vnoremap YY :y<CR>; 需另一个feature
    val mode = CommandState.getInstance(editor).getSubMode();
    val selection = SelectionType.fromSubMode(mode);

    val ret = injector.yank.yankRange(editor, TextRange(startsArray, endsArray), selection, true)
    val register = VimPlugin.getRegister()
    val systemRegister = register.getRegister(register.defaultRegister)
    if (systemRegister != null) {
      val text = systemRegister.text
      val transferableData = arrayListOf<TextBlockTransferableData>()
      ClipboardHandler.setClipboardText(text, transferableData, text)
    }
    return ret
  }
}
