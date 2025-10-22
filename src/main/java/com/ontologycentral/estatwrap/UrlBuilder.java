package com.ontologycentral.estatwrap;

/**
 * Utility class for building URLs for Eurostat SDMX APIs.
 */
public final class UrlBuilder {

    /**
     * Builds URL for concept scheme.
     *
     * @param id the concept scheme ID
     * @return complete API URL
     */
    public static String buildConceptSchemeUrl(String id) {
        return ApiConstants.URI_PREFIX_3 + "/structure/conceptscheme/ESTAT/" + id;
    }

    /**
     * Builds URL for code list.
     *
     * @param id the code list ID
     * @return complete API URL
     */
    public static String buildCodeListUrl(String id) {
        return ApiConstants.URI_PREFIX_3 + "/structure/codelist/ESTAT/" + id.toUpperCase();
    }

    /**
     * Builds URL for data structure definition.
     *
     * @param id the data structure ID
     * @return complete API URL
     */
    public static String buildDataStructureUrl(String id) {
        return ApiConstants.URI_PREFIX_3 + "/structure/datastructure/ESTAT/" + id;
    }

    /**
     * Builds URL for dataflow definition.
     *
     * @param id the dataflow ID
     * @return complete API URL
     */
    public static String buildDataflowUrl(String id) {
        return ApiConstants.URI_PREFIX_3 + "/structure/dataflow/ESTAT/" + id;
    }

    /**
     * Builds URL for data constraint definition.
     *
     * @param id the data constraint ID
     * @return complete API URL
     */
    public static String buildDataConstraintUrl(String id) {
        return ApiConstants.URI_PREFIX_3 + "/structure/dataconstraint/ESTAT/" + id;
    }

    /**
     * Builds URL for data observations using SDMX 3.0 API.
     *
     * @param id the dataset ID
     * @return complete API URL
     */
    public static String buildDataUrl(String id) {
        return ApiConstants.URI_PREFIX_3 + "/data/dataflow/ESTAT/" + id + "/1.0?format=tsv&compress=false";
    }

    /**
     * Builds URL for legacy TSV data using SDMX 2.1 API.
     *
     * @param id the dataset ID
     * @return complete API URL
     */
    public static String buildLegacyDataUrl(String id) {
        return ApiConstants.URI_PREFIX_21 + "/data/" + id + "/?format=TSV&compressed=true";
    }

    /**
     * Gets the table of contents XML URL.
     *
     * @return table of contents URL
     */
    public static String getTocUrl() {
        return ApiConstants.TOC_XML_URL;
    }

    private UrlBuilder() {
        // Utility class
    }
}