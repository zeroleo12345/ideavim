/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */
package com.maddyhome.idea.vim.action.copy

import com.intellij.vim.annotations.CommandOrMotion
import com.intellij.vim.annotations.Mode
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.handler.VimActionHandler
import com.maddyhome.idea.vim.diagnostic.debug
import com.maddyhome.idea.vim.diagnostic.vimLogger

@CommandOrMotion(keys = [";"], modes = [Mode.NORMAL])
public class YankToClipboard : VimActionHandler.SingleExecution() {  // 抄类: class YankLineAction
  private val logger = vimLogger<YankToClipboard>()

  override val type: Command.Type = Command.Type.COPY

  override fun execute(
    editor: VimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    // 修改后
    this.logger.debug { "YankToClipboard execute" } // 从磁盘重新安装插件可生效, 注意需删热键 ; 映射动作 MotionLastMatchCharAction
    val registerService = injector.registerGroup
    val systemRegister = registerService.getRegister(registerService.defaultRegister)
    if (systemRegister != null) {
      val text = systemRegister.text
      if (text != null) {
        val transferableData: List<Any> = ArrayList()
        injector.clipboardManager.setClipboardText(text, text, ArrayList(transferableData))
        return true
      }
    }
    return false
  }
}
