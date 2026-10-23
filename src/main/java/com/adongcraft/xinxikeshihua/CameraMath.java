package com.adongcraft.xinxikeshihua;

/** 屏幕坐标系：以眼睛为原点，前、右、上三个方向展开。 */
public final class CameraMath {
    public record Point(double x, double y, double z) {
    }

    private CameraMath() {
    }

    /**
     * @param yaw    Minecraft 水平朝向，0 为 +Z，90 为 -X
     * @param pitch  Minecraft 俯仰，-90 为抬头看天
     * @param right  向右偏移，正值为玩家的右手方向
     * @param up     向上偏移
     */
    public static Point offset(double eyeX, double eyeY, double eyeZ, float yaw, float pitch,
                               double distance, double right, double up) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double cosPitch = Math.cos(pitchRadians);

        double forwardX = -Math.sin(yawRadians) * cosPitch;
        double forwardY = -Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * cosPitch;

        // right = normalize(forward × worldUp)；抬头看天时退回到由 yaw 决定的方向。
        double rightX = -forwardZ;
        double rightZ = forwardX;
        double length = Math.hypot(rightX, rightZ);
        if (length < 1.0E-6D) {
            rightX = -Math.cos(yawRadians);
            rightZ = -Math.sin(yawRadians);
        } else {
            rightX /= length;
            rightZ /= length;
        }

        // up = right × forward
        double upX = -rightZ * forwardY;
        double upY = rightZ * forwardX - rightX * forwardZ;
        double upZ = rightX * forwardY;

        return new Point(
                eyeX + forwardX * distance + rightX * right + upX * up,
                eyeY + forwardY * distance + upY * up,
                eyeZ + forwardZ * distance + rightZ * right + upZ * up
        );
    }
}
