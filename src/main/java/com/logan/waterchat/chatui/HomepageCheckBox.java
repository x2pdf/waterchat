package com.logan.waterchat.chatui;


import com.logan.waterchat.chat.ChatRoleEnum;
import com.logan.waterchat.chat.MessageDTO;
import com.logan.waterchat.chat.SessionCtrl;
import com.logan.waterchat.config.SysConfig;
import com.logan.waterchat.config.SysConfigAction;
import com.logan.waterchat.utils.LogUtils;
import javafx.scene.control.CheckBox;

public class HomepageCheckBox {

    public static CheckBox getIsNeedSystemPrompt() {
        CheckBox isNeedSystemPrompt = new CheckBox(SysConfigAction.getLang("isNeedSystemPrompt"));
        isNeedSystemPrompt.setSelected(true);   // 这句让它默认勾选
        isNeedSystemPrompt.setOnAction(e -> {
            if (isNeedSystemPrompt.isSelected()) {
                HomepageAdaptor.IS_NEED_SYSTEM_PROMPT = true;
                boolean isHasSystemPrompt = false;
                for (MessageDTO messageDTO : SessionCtrl.messageDTOS) {
                    if (messageDTO.getRole().equals(ChatRoleEnum.system)) {
                        isHasSystemPrompt = true;
                    }
                }
                if (!isHasSystemPrompt) {
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setRole(ChatRoleEnum.system);
                    messageDTO.setContent(SysConfig.MODEL_DEFAULT_SYSTEM_PROMPT);
                    SessionCtrl.messageDTOS.add(0, messageDTO);
                }
                LogUtils.info("isNeedSystemPrompt 功能已启用");
            } else {
                HomepageAdaptor.IS_NEED_SYSTEM_PROMPT = false;
                MessageDTO messageDTO = SessionCtrl.messageDTOS.get(0);
                if (messageDTO.getRole().equals(ChatRoleEnum.system)) {
                    SessionCtrl.messageDTOS.remove(0);
                }
                LogUtils.info("isNeedSystemPrompt 功能已关闭");
            }

            HomepageAdaptor.freshChatMsgBox();
        });

        return isNeedSystemPrompt;
    }


    public static CheckBox getEnableThinking() {
        CheckBox enableThinking = new CheckBox(SysConfigAction.getLang("thinkingMode"));
        enableThinking.setOnAction(e -> {
            if (enableThinking.isSelected()) {
                HomepageAdaptor.ENABLE_THINKING = true;
                LogUtils.info("Thinking 功能已启用");
            } else {
                HomepageAdaptor.ENABLE_THINKING = false;
                LogUtils.info("Thinking 功能已关闭");
            }
        });
        return enableThinking;
    }

}
