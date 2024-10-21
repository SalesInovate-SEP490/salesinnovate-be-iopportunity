package fpt.capstone.iOpportunity.util;

public interface AppConst {

    String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    String SEARCH_SPEC_OPERATOR = "(\\w+?)([><!~*:`@])\\s*(.*)";
    String SORT_BY = "(\\w+?)(:)(.*)";
    String FORECAST_REGEX = "industry_";
    String STAGE_REGEX = "rating_";
    String SOURCE_REGEX = "source_";
    String TYPE_REGEX = "type_";
    String FAMILY_REGEX ="family_";
    String STATUS_REGEX = "status_";
}
