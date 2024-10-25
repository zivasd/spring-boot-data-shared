package io.github.zivasd.spring.boot.data.shared.query;

import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.lang.Nullable;

public class SharedParametersParameterAccessor extends ParametersParameterAccessor {

	public SharedParametersParameterAccessor(Parameters<?, ?> parameters, Object[] values) {
		super(parameters, values);
	}

	@Nullable
	public <T> T getValue(Parameter parameter) {
		return super.getValue(parameter.getIndex());
	}
}
