package com.windanesz.sisyphus.packet;

import com.windanesz.sisyphus.SisyphusBackport;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> The server options that the client follows instead of its own, sent on join and when they change.
 */
public class PacketServerSettings implements IMessageHandler<PacketServerSettings.Message, IMessage> {

    @Override
    public IMessage onMessage(Message message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            SisyphusBackport.proxy.handleServerSettingsPacket(message);
        }
        return null;
    }

    public static class Message implements IMessage {

        public boolean showBrushingInJei = true;
        public int discWindUp;

        public Message() {
        }

        public Message(boolean showBrushingInJei, int discWindUp) {
            this.showBrushingInJei = showBrushingInJei;
            this.discWindUp = discWindUp;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.showBrushingInJei = buf.readBoolean();
            this.discWindUp = buf.readUnsignedByte();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(this.showBrushingInJei);
            buf.writeByte(this.discWindUp);
        }
    }
}
