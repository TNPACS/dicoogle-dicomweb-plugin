package com.tnpacs.dicoogle.dicomwebplugin.utils;

import java.util.regex.Pattern;

public class Patterns {
    /* BEGIN QUERY */
    public static final Pattern QUERY_STUDIES = Pattern.compile("/studies/?");
    public static final Pattern QUERY_SERIES = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/?", PathParameters.STUDY_INSTANCE_UID));
    public static final Pattern QUERY_INSTANCES = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/(?<%s>[0-9.]+)/instances/?",
                    PathParameters.STUDY_INSTANCE_UID, PathParameters.SERIES_INSTANCE_UID));
    /* END QUERY */

    /* BEGIN RETRIEVE */
    public static final Pattern RETRIEVE_STUDY = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/?", PathParameters.STUDY_INSTANCE_UID));
    public static final Pattern RETRIEVE_SERIES = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/(?<%s>[0-9.]+)/?",
                    PathParameters.STUDY_INSTANCE_UID, PathParameters.SERIES_INSTANCE_UID));
    public static final Pattern RETRIEVE_INSTANCE = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/(?<%s>[0-9.]+)/instances/(?<%s>[0-9.]+)/?",
                    PathParameters.STUDY_INSTANCE_UID, PathParameters.SERIES_INSTANCE_UID, PathParameters.SOP_INSTANCE_UID));

    public static final Pattern RETRIEVE_STUDY_METADATA = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/metadata/?",
                    PathParameters.STUDY_INSTANCE_UID));
    public static final Pattern RETRIEVE_SERIES_METADATA = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/(?<%s>[0-9.]+)/metadata/?",
                    PathParameters.STUDY_INSTANCE_UID, PathParameters.SERIES_INSTANCE_UID));
    public static final Pattern RETRIEVE_INSTANCE_METADATA = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/(?<%s>[0-9.]+)/instances/(?<%s>[0-9.]+)/metadata/?",
                    PathParameters.STUDY_INSTANCE_UID, PathParameters.SERIES_INSTANCE_UID, PathParameters.SOP_INSTANCE_UID));
    public static final Pattern RETRIEVE_INSTANCE_FRAMES = Pattern.compile(
            String.format("/studies/(?<%s>[0-9.]+)/series/(?<%s>[0-9.]+)/instances/(?<%s>[0-9.]+)/frames/(?<%s>\\d+)",
                    PathParameters.STUDY_INSTANCE_UID, PathParameters.SERIES_INSTANCE_UID, PathParameters.SOP_INSTANCE_UID,
                    PathParameters.FRAME_LIST));
    /* END RETRIEVE */
}
