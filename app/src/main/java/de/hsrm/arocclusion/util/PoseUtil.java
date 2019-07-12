package de.hsrm.arocclusion.util;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class PoseUtil {

    public static Pose getPose(Vector3 v, Quaternion q) {
        return new Pose(new float[]{v.x, v.y, v.z}, new float[]{q.x, q.y, q.z, q.w});
    }

    public static Vector3 getVector(Pose pose) {
        return new Vector3(pose.tx(), pose.ty(), pose.tz());
    }

    public static Quaternion getQuaternion(Pose pose) {
        return new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw());
    }
}
