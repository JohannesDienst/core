package com.dotmarketing.portlets.rules.conditionlet;

import com.dotcms.util.HttpRequestDataUtil;
import com.dotmarketing.portlets.rules.RuleComponentInstance;
import com.dotmarketing.portlets.rules.exception.ComparisonNotPresentException;
import com.dotmarketing.portlets.rules.exception.ComparisonNotSupportedException;
import com.dotmarketing.portlets.rules.model.ParameterModel;
import com.dotmarketing.portlets.rules.parameter.ParameterDefinition;
import com.dotmarketing.portlets.rules.parameter.comparison.Comparison;
import com.dotmarketing.portlets.rules.parameter.display.TextInput;
import com.dotmarketing.portlets.rules.parameter.type.TextType;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.CONTAINS;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.ENDS_WITH;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.IS;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.IS_NOT;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.REGEX;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.STARTS_WITH;

/**
 * This conditionlet will allow CMS users to check the current URL in a request.
 * The comparison of URLs is case-insensitive, except for the regular expression
 * comparison. This {@link Conditionlet} provides a drop-down menu with the
 * available comparison mechanisms, and a text field to enter the value to
 * compare. The URL input value is required.
 *
 * As part of dotCMS functionality the 'CMS_INDEX_PAGE' property is used to imply the 'index'
 * value of a directory, so if a directory such as '/contact-us/' is used on the
 * conditionlet remember to by check if the directory has an index page set, if
 * it does the conditionlet should test against '/contact-us/index' to evaluate
 * the URL correctly
 *
 */
public class UsersCurrentUrlConditionlet extends Conditionlet<UsersCurrentUrlConditionlet.Instance> {

	private static final long serialVersionUID = 1L;

	public static final String PATTERN_URL_INPUT_KEY = "current-url";

	public UsersCurrentUrlConditionlet() {
		super("api.ruleengine.system.conditionlet.UsersCurrentUrl", new ComparisonParameterDefinition(2, IS, IS_NOT,
                STARTS_WITH, ENDS_WITH, CONTAINS, REGEX), patternUrl);
	}

	private static final ParameterDefinition<TextType> patternUrl = new ParameterDefinition<>(3, PATTERN_URL_INPUT_KEY,
            new TextInput<TextType>(new TextType().minLength(1)));

	@Override
    public boolean evaluate(HttpServletRequest request, HttpServletResponse response, Instance instance) {
        String requestUri = null;
		try {
			requestUri = HttpRequestDataUtil.getUri(request);
		} catch (UnsupportedEncodingException e) {
			Logger.error(this, "Could not retrieved a valid URI from request: "
					+ request.getRequestURL());
		}
		if (!UtilMethods.isSet(requestUri)) {
			return false;
		}
		return instance.comparison.perform(requestUri, instance.patternUrl);
	}

    @Override
    public Instance instanceFrom(Map<String, ParameterModel> parameters) {
    	return new Instance(this, parameters);
    }

    public static class Instance implements RuleComponentInstance {
    	private final String patternUrl;
        private final Comparison<String> comparison;
        private final String comparisonValue;

        private Instance(UsersCurrentUrlConditionlet definition, Map<String, ParameterModel> parameters) {
            this.patternUrl = parameters.get(PATTERN_URL_INPUT_KEY).getValue();
            this.comparisonValue = parameters.get(COMPARISON_KEY).getValue();

            try {
                this.comparison = ((ComparisonParameterDefinition) definition.getParameterDefinitions().get(
                        COMPARISON_KEY)).comparisonFrom(comparisonValue);
            } catch (ComparisonNotPresentException e) {
                throw new ComparisonNotSupportedException(
                        "The comparison '%s' is not supported on Condition type '%s'", comparisonValue,
                        definition.getId());
            }
        }
    }
}
