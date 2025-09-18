package xland.mcmod.neospeedzero.util;

import com.mojang.serialization.JavaOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.action.CommandTemplate;
import net.minecraft.server.dialog.action.ParsedTemplate;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class DialogUtil {
    public static ItemBody itemBody(ItemStack icon, Component description) {
        PlainMessage plainMessage = new PlainMessage(description, PlainMessage.DEFAULT_WIDTH);
        return new ItemBody(icon, Optional.of(plainMessage), true, true, 16, 16);
    }

    public static CommonDialogData commonDialogData(Component title, boolean pause, List<DialogBody> body) {
        return new CommonDialogData(
                title, Optional.empty(),
                true, pause, DialogAction.CLOSE,
                body, Collections.emptyList()
        );
    }

    public static CommonDialogData commonDialogData(Component title, List<Input> inputs) {
        return new CommonDialogData(
                title, Optional.empty(),
                true, true, DialogAction.CLOSE,
                Collections.emptyList(), inputs
        );
    }

    public static SingleOptionInput singleOptionInput(Component label, List<SingleOptionInput.Entry> entries) {
        return new SingleOptionInput(200, entries, label, true);
    }

    public static CommonButtonData commonButtonData(Component text) {
        return new CommonButtonData(text, 150);
    }

    public static CommandTemplate commandTemplate(String template) {
        // M J S B: why not open a constructor(String)
        return new CommandTemplate(
                ParsedTemplate.CODEC.parse(JavaOps.INSTANCE, template).getOrThrow(/* should not happen */)
        );
    }

    private DialogUtil() {}
}
