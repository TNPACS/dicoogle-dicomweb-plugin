package com.tnpacs.dicoogle.dicomwebplugin.utils;

import org.dcm4che2.data.Tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static final String MONGO_QUERY_PLUGIN_NAME = "mongo-query-plugin";
    public static final String MONGO_STORAGE_PLUGIN_NAME = "mongo-storage-plugin";

    public static final String PARAM_INCLUDE_FIELD = "includefield";
    public static final String PARAM_FUZZY_MATCHING = "fuzzymatching";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_OFFSET = "offset";
    public static final String METADATA_VR_MAP = "VRMap";
    public static final String TAG_VR = "vr";
    public static final String TAG_VALUE = "Value";
    public static final String TAG_PN_ALPHABETIC = "Alphabetic";

    /* ATTRIBUTES http://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_10.6.3.3 */
    public static final String RETRIEVE_URL_NAME = "RetrieveURL";
    public static final int RETRIEVE_URL_TAG = 528784;
    public static final String RETRIEVE_URL_VR = "UR";

//    public static final String PIXEL_DATA_NAME = "PixelData";
//    public static final int PIXEL_DATA_TAG = 2145386512;
//    public static final int PIXEL_DATA_UR


    public static final Set<String> STUDY_LEVEL_ATTRIBUTES = new HashSet<>(
            Arrays.asList(
                    Dictionary.getInstance().getName(Tag.StudyDate),
                    Dictionary.getInstance().getName(Tag.StudyTime),
                    Dictionary.getInstance().getName(Tag.AccessionNumber),
                    Dictionary.getInstance().getName(Tag.InstanceAvailability),
                    Dictionary.getInstance().getName(Tag.ModalitiesInStudy),
                    Dictionary.getInstance().getName(Tag.ReferringPhysicianName),
                    Dictionary.getInstance().getName(Tag.TimezoneOffsetFromUTC),
                    Dictionary.getInstance().getName(Tag.PatientName),
                    Dictionary.getInstance().getName(Tag.PatientID),
                    Dictionary.getInstance().getName(Tag.PatientBirthDate),
                    Dictionary.getInstance().getName(Tag.PatientSex),
                    Dictionary.getInstance().getName(Tag.StudyInstanceUID),
                    Dictionary.getInstance().getName(Tag.StudyID),
                    Dictionary.getInstance().getName(Tag.NumberOfStudyRelatedSeries),
                    Dictionary.getInstance().getName(Tag.NumberOfStudyRelatedInstances)
            )
    );
    public static final Set<String> SERIES_LEVEL_ATTRIBUTES = new HashSet<>(
            Arrays.asList(
                    Dictionary.getInstance().getName(Tag.Modality),
                    Dictionary.getInstance().getName(Tag.TimezoneOffsetFromUTC),
                    Dictionary.getInstance().getName(Tag.SeriesDescription),
                    Dictionary.getInstance().getName(Tag.SeriesInstanceUID),
                    Dictionary.getInstance().getName(Tag.SeriesNumber),
                    Dictionary.getInstance().getName(Tag.NumberOfSeriesRelatedInstances),
                    Dictionary.getInstance().getName(Tag.PerformedProcedureStepStartDate),
                    Dictionary.getInstance().getName(Tag.PerformedProcedureStepStartTime),
                    Dictionary.getInstance().getName(Tag.RequestAttributesSequence),
                    Dictionary.getInstance().getName(Tag.ScheduledProcedureStepID),
                    Dictionary.getInstance().getName(Tag.RequestedProcedureID)
            )
    );
    public static final Set<String> INSTANCE_LEVEL_ATTRIBUTES = new HashSet<>(
            Arrays.asList(
                    Dictionary.getInstance().getName(Tag.SOPClassUID),
                    Dictionary.getInstance().getName(Tag.SOPInstanceUID),
                    Dictionary.getInstance().getName(Tag.InstanceAvailability),
                    Dictionary.getInstance().getName(Tag.TimezoneOffsetFromUTC),
                    Dictionary.getInstance().getName(Tag.InstanceNumber),
                    Dictionary.getInstance().getName(Tag.Rows),
                    Dictionary.getInstance().getName(Tag.Columns),
                    Dictionary.getInstance().getName(Tag.BitsAllocated),
                    Dictionary.getInstance().getName(Tag.NumberOfFrames)
            )
    );
}
