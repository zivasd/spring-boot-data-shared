package io.github.zivasd.spring.boot.data.shared.query;

import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.lang.Nullable;

import io.github.zivasd.spring.boot.data.shared.repository.TableNameDecider;

public class SharedParametersParameterAccessor extends ParametersParameterAccessor {

	public SharedParametersParameterAccessor(Parameters<?, ?> parameters, Object[] values) {
		super(parameters, values);
	}

	@Nullable
	public <T> T getValue(Parameter parameter) {
		return super.getValue(parameter.getIndex());
	}

	@Override
	public SharedParameters getParameters() {
		return (SharedParameters) super.getParameters();
	}

	public TableNameDecider getTableNameDecider() {
		if (!getParameters().hasTableNameDecider()) {
			return TableNameDecider.NoOperator.INSTANCE;
		}
		TableNameDecider decider = (TableNameDecider) getValue(getParameters().getTableNameDeciderIndex());
		return decider == null ? TableNameDecider.NoOperator.INSTANCE : decider;
	}
}
