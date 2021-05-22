package ai.group.snapchat_filter.Utils;

public final class Constants {

    //Back or Front of the mobile camera.
    public enum CameraPosition{
        Back(-1),
        Front(1);

        private int mValue;
        private CameraPosition(int value){this.mValue = value;}
        public int toInt(){return this.mValue;}
    }

}
