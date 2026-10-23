package com.adongcraft.xinxikeshihua;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * 把左上方那一块 HUD 换算成相机前方的坐标。
 *
 * <p>文字显示实体以自身位置为垂直中心，所以整块文字的中心要按行数往回收，
 * 这样最上面一行始终停在 anchor-y 上。图标和第三行（需求工具）对齐。
 */
public final class HudLayout {
    public record Layout(
            CameraMath.Point text,
            List<CameraMath.Point> lines,
            CameraMath.Point blockIcon,
            CameraMath.Point toolIcon,
            double iconScale
    ) {
    }

    private HudLayout() {
    }

    public static Layout compute(Location eye, Settings settings, int lineCount,
                                 boolean showBlockIcon, boolean showToolIcon) {
        int lines = Math.max(1, lineCount);
        double distance = settings.hudDistance();
        double lineHeight = settings.lineHeightFactor() * settings.hudScale();
        double topY = settings.anchorY();
        double centerY = topY - (lines - 1) * lineHeight / 2.0D;

        CameraMath.Point text = offset(eye, distance, settings.anchorX(), centerY);
        List<CameraMath.Point> linePoints = new ArrayList<>(lines);
        for (int index = 0; index < lines; index++) {
            linePoints.add(offset(eye, distance, settings.anchorX(), topY - index * lineHeight));
        }

        double iconScale = lineHeight * 1.25D;
        double iconX = settings.anchorX() + iconScale / 2.0D + lineHeight * 0.15D;
        CameraMath.Point blockIcon = showBlockIcon
                ? offset(eye, distance - 0.006D, iconX, topY)
                : null;
        CameraMath.Point toolIcon = showToolIcon && lines >= 3
                ? offset(eye, distance - 0.006D, iconX, topY - 2.0D * lineHeight)
                : null;

        return new Layout(text, List.copyOf(linePoints), blockIcon, toolIcon, iconScale);
    }

    private static CameraMath.Point offset(Location eye, double distance, double right, double up) {
        return CameraMath.offset(eye.getX(), eye.getY(), eye.getZ(), eye.getYaw(), eye.getPitch(),
                distance, right, up);
    }
}
