package com.tnpacs.dicoogle.dicomwebplugin.utils;

import org.dcm4che2.data.Tag;

public class PathParameters {
    public static final String STUDY_INSTANCE_UID = Dictionary.getInstance().getName(Tag.StudyInstanceUID);
    public static final String SERIES_INSTANCE_UID = Dictionary.getInstance().getName(Tag.SeriesInstanceUID);
    public static final String SOP_INSTANCE_UID = Dictionary.getInstance().getName(Tag.SOPInstanceUID);
    public static final String FRAME_LIST = "FrameList";
}
