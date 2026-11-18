package xy177.combatrollantique.config;

import net.minecraftforge.common.config.Config;
import xy177.combatrollantique.CombatRollAntique;

@Config(modid = CombatRollAntique.MOD_ID, category = "")
public final class RollConfig {
    public static Server server = new Server();
    public static Client client = new Client();
    public static Hud hud = new Hud();
    @Config.Ignore private static Server remoteServer;

    public static Server server() { return remoteServer == null ? server : remoteServer; }
    public static void setRemote(Server config) { remoteServer = config; }

    public static class Server {
        @Config.Comment({"翻滚时获得的无敌时间，单位为游戏刻。", "Number of invulnerable game ticks granted upon rolling."})
        @Config.RangeInt(min = 0)
        public int invulnerable_ticks_upon_roll = 0;
        @Config.Comment({"翻滚持续时间，期间不能攻击、使用物品或手动跳跃。", "Roll duration in ticks, during which attacking, item use and manual jumping are blocked."})
        @Config.RangeInt(min = 1)
        public int roll_duration = 8;
        @Config.Comment({"允许翻滚时触发原版自动跳跃。", "Allow vanilla auto-jump while rolling."})
        public boolean allow_auto_jump_while_rolling = true;
        @Config.Comment({"允许翻滚时手动跳跃；会影响距离属性和跃进附魔的效果。", "Allow manual jumping while rolling; this affects the distance attribute and Longfooted enchantment."})
        public boolean allow_jump_while_rolling = false;
        @Config.Comment({"允许武器攻击冷却未结束时翻滚。", "Allow rolling while the weapon attack cooldown is active."})
        public boolean allow_rolling_while_weapon_cooldown = false;
        @Config.Comment({"允许在空中翻滚；会影响距离属性和跃进附魔的效果。", "Allow rolling in midair; this affects the distance attribute and Longfooted enchantment."})
        public boolean allow_rolling_while_airborn = false;
        @Config.Comment({"每次翻滚增加的饥饿消耗。", "Exhaustion added for each roll."})
        @Config.RangeDouble(min = 0)
        public float exhaust_on_roll = 0.1F;
        @Config.Comment({"饥饿值必须高于此值才能翻滚。", "Food level must be above this value to roll."})
        public float food_level_required = 6;
        @Config.Comment({"每次翻滚的冷却时间，单位为秒。", "Cooldown per roll, in seconds."})
        @Config.RangeDouble(min = 0)
        public float roll_cooldown = 4;
        @Config.Comment({"额外翻滚距离；不参与属性倍率计算，默认距离为 3。", "Additional roll distance, unaffected by attribute multipliers; the default distance is 3."})
        public float additional_roll_distance = 0;
    }

    public static class Client {
        @Config.Comment({"播放翻滚冷却结束音效。", "Play the cooldown-ready sound."})
        public boolean playCooldownSound = true;
        @Config.Comment({"冷却即将结束时闪烁箭头。", "Flash the arrow when a roll is nearly recharged."})
        public boolean playCooldownFlash = true;
        @Config.Comment({"播放翻滚音效。", "Play the roll sound."})
        public boolean playRollSound = true;
        @Config.Comment({"箭头的 RGB 颜色值。", "RGB color of the HUD arrow."})
        public int hudArrowColor = 0x5488e3;
        @Config.Comment({"箭头背景不透明度，范围为 0 到 100。", "HUD arrow background opacity, from 0 to 100."})
        @Config.RangeInt(min = 0, max = 100)
        public int hudBackgroundOpacity = 75;
        @Config.Comment({"翻滚次数充满时仍显示箭头。", "Show the arrow when all rolls are available."})
        public boolean showWhenFull = true;
        @Config.Comment({"在创造模式下显示箭头。", "Show the HUD in creative mode."})
        public boolean showHUDInCreative = false;
        @Config.Comment({"在箭头旁显示翻滚按键。", "Show the roll keybinding beside the arrow."})
        public boolean showKeybinding = true;
        @Config.Comment({"按键标签的位置：箭头上方或左侧。", "Position of the key label: above or left of the arrow."})
        public LabelPosition keybindingLabelPosition = LabelPosition.LEFT;
    }
    public enum LabelPosition { TOP, LEFT }
    public enum Origin { TOP, TOP_LEFT, TOP_RIGHT, BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT }
    public static class Hud {
        @Config.Comment({"箭头位置的屏幕锚点。", "Screen anchor for the arrow position."})
        public Origin origin = Origin.BOTTOM;
        @Config.Comment({"箭头相对锚点的水平偏移。", "Horizontal arrow offset from the anchor."})
        public int x = 108;
        @Config.Comment({"箭头相对锚点的垂直偏移。", "Vertical arrow offset from the anchor."})
        public int y = -12;
    }
}
