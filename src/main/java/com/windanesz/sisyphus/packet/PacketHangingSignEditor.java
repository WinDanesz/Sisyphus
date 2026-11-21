package com.windanesz.sisyphus.packet;

import com.windanesz.sisyphus.SisyphusBackport;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> Opens the editing screen of a hanging sign that was just placed.
 */
public class PacketHangingSignEditor implements IMessageHandler<PacketHangingSignEditor.Message, IMessage> {

    @Override
    public IMessage onMessage(Message message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            SisyphusBackport.proxy.handleHangingSignEditorPacket(message);
        }
        return null;
    }

    public static class Message implements IMessage {

        public BlockPos pos = BlockPos.ORIGIN;
        public String wood = "";

        public Message() {
        }

        public Message(BlockPos pos, String wood) {
            this.pos = pos;
            this.wood = wood;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.pos = BlockPos.fromLong(buf.readLong());
            this.wood = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(this.pos.toLong());
            ByteBufUtils.writeUTF8String(buf, this.wood);
        }
    }
}
