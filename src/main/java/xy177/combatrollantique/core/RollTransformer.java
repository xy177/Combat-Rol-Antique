package xy177.combatrollantique.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Small call-site adapters for the 1.12 renderer, input loop and anvil. */
public final class RollTransformer implements IClassTransformer,Opcodes {
    private static final String CLIENT="xy177/combatrollantique/client/ClientProxy";
    private static final String RENDER="xy177/combatrollantique/client/RenderHooks";
    @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
        if (bytes==null) return null;
        boolean model="net.minecraft.client.model.ModelBiped".equals(transformedName);
        boolean render="net.minecraft.client.renderer.entity.RenderPlayer".equals(transformedName);
        boolean input="net.minecraft.client.Minecraft".equals(transformedName);
        boolean player="net.minecraft.client.entity.EntityPlayerSP".equals(transformedName);
        boolean anvil="net.minecraft.inventory.ContainerRepair".equals(transformedName);
        if (!model && !render && !input && !player && !anvil) return bytes;
        ClassNode type=new ClassNode(); new ClassReader(bytes).accept(type,0);
        int changed=0;
        for (MethodNode method:type.methods) {
            if (model && named(method,"setRotationAngles","func_78087_a") && method.desc.equals("(FFFFFFLnet/minecraft/entity/Entity;)V")) {
                InsnList before=new InsnList(); before.add(new VarInsnNode(ALOAD,0));before.add(new VarInsnNode(ALOAD,7));
                before.add(call(RENDER,"beforeAngles","(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V"));method.instructions.insert(before);
                for (AbstractInsnNode insn:method.instructions.toArray()) if (insn.getOpcode()==RETURN) {
                    InsnList after=new InsnList();after.add(new VarInsnNode(ALOAD,0));
                    after.add(call(RENDER,"afterAngles","(Lnet/minecraft/client/model/ModelBiped;)V"));method.instructions.insertBefore(insn,after);
                }
                changed++;
            }
            if (render && named(method,"applyRotations","func_77043_a") && method.desc.equals("(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V")) {
                for (AbstractInsnNode insn:method.instructions.toArray()) if (insn.getOpcode()==RETURN) {
                    method.instructions.insertBefore(insn,call(RENDER,"body","()V"));
                }
                changed++;
            }
            if (input && (named(method,"clickMouse","func_147116_af") || named(method,"rightClickMouse","func_147121_ag") || named(method,"sendClickBlockToController","func_147115_a"))) {
                guard(method,"blockActions",false); changed++;
            }
            if (input && named(method,"processKeyBinds","func_184117_aA")) {
                for (AbstractInsnNode insn:method.instructions.toArray()) if (insn.getOpcode()==RETURN) method.instructions.insertBefore(insn,call(CLIENT,"tryRolling","()V"));
                changed++;
            }
            if (player && named(method,"onLivingUpdate","func_70636_d")) {
                for (AbstractInsnNode insn:method.instructions.toArray()) if (insn instanceof MethodInsnNode) {
                    MethodInsnNode invocation=(MethodInsnNode)insn;
                    if ("updatePlayerMoveState".equals(invocation.name) || "func_78898_a".equals(invocation.name)) {
                        InsnList after=new InsnList();after.add(new VarInsnNode(ALOAD,0));after.add(call(CLIENT,"adjustInput","(Lnet/minecraft/client/entity/EntityPlayerSP;)V"));
                        method.instructions.insert(insn,after);changed++;
                    }
                }
            }
            if (player && named(method,"isAutoJumpEnabled","func_189809_N")) { guard(method,"blockAutoJump",true);changed++; }
            if (anvil && named(method,"updateRepairOutput","func_82848_d")) changed+=anvilCost(method);
        }
        int expected=model||render||anvil?1:player?2:4;
        if (changed!=expected) throw new IllegalStateException("Combat Roll Antique: expected "+expected+" hooks in "+transformedName+", found "+changed);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);type.accept(writer);return writer.toByteArray();
    }
    private static boolean named(MethodNode method,String mcp,String srg) { return mcp.equals(method.name)||srg.equals(method.name); }
    private static MethodInsnNode call(String owner,String method,String desc) { return new MethodInsnNode(INVOKESTATIC,owner,method,desc,false); }
    private static void guard(MethodNode method,String predicate,boolean booleanResult) {
        InsnList head=new InsnList();LabelNode proceed=new LabelNode();
        head.add(call(CLIENT,predicate,"()Z"));head.add(new JumpInsnNode(IFEQ,proceed));
        if (booleanResult) head.add(new InsnNode(ICONST_0));
        head.add(new InsnNode(booleanResult?IRETURN:RETURN));head.add(proceed);head.add(new FrameNode(F_SAME,0,null,0,null));
        method.instructions.insert(head);
    }
    private static int anvilCost(MethodNode method) {
        for (AbstractInsnNode insn:method.instructions.toArray()) if (insn instanceof MethodInsnNode) {
            MethodInsnNode invocation=(MethodInsnNode)insn;
            if (!("getRarity".equals(invocation.name)||"func_77324_c".equals(invocation.name))) continue;
            AbstractInsnNode load=insn.getPrevious();
            if (!(load instanceof VarInsnNode) || load.getOpcode()!=ALOAD) continue;
            int enchantment=((VarInsnNode)load).var;
            AbstractInsnNode next=insn.getNext();while (next!=null && !(next instanceof TableSwitchInsnNode)) next=next.getNext();
            if (next==null) continue;
            TableSwitchInsnNode cases=(TableSwitchInsnNode)next;
            AbstractInsnNode store=cases.labels.get(0).getNext();while (store!=null && store.getOpcode()!=ISTORE) store=store.getNext();
            if (!(store instanceof VarInsnNode)) continue;
            int cost=((VarInsnNode)store).var;
            AbstractInsnNode end=cases.dflt;while (end.getNext()!=null && end.getNext().getOpcode()<0) end=end.getNext();
            InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,enchantment));hook.add(new VarInsnNode(ILOAD,cost));
            hook.add(call("xy177/combatrollantique/common/RollContent","anvilCost","(Lnet/minecraft/enchantment/Enchantment;I)I"));hook.add(new VarInsnNode(ISTORE,cost));
            method.instructions.insert(end,hook);return 1;
        }
        return 0;
    }
}
