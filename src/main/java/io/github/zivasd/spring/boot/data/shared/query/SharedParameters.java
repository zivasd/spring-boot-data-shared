package io.github.zivasd.spring.boot.data.shared.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import io.github.zivasd.spring.boot.data.shared.query.SharedParameters.SharedParameter;
import io.github.zivasd.spring.boot.data.shared.repository.TableNameDecider;

public class SharedParameters extends Parameters<SharedParameters, SharedParameter> {
    private final int tableNameDeciderIndex;

    public SharedParameters(Method method) {
        super(method);
        tableNameDeciderIndex = findTableNameDeciderIndex(this);
    }

    private SharedParameters(List<SharedParameter> parameters) {
        super(parameters);
        tableNameDeciderIndex = findTableNameDeciderIndex(this);
    }

    @Override
    protected SharedParameter createParameter(MethodParameter parameter) {
        return new SharedParameter(parameter);
    }

    @Override
    protected SharedParameters createFrom(List<SharedParameter> parameters) {
        return new SharedParameters(parameters);
    }

    public boolean hasTableNameDecider() {
        return tableNameDeciderIndex != -1;
    }

    public int getTableNameDeciderIndex() {
        return tableNameDeciderIndex;
    }

    public SharedParameters getDeciderParamParameter() {
        List<SharedParameter> deciderParams = new ArrayList<>();
        for (SharedParameter candidate : this) {
            if (candidate.isDeciderParamParameter()) {
                deciderParams.add(candidate);
            }
        }
        return createFrom(deciderParams);
    }

    private int findTableNameDeciderIndex(SharedParameters parameters) {
        int index = -1;
        for (SharedParameter candidate : parameters) {
            if (candidate.isTableNameDeciderParameter()) {
                index = candidate.getIndex();
                break;
            }
        }
        return index;
    }

    public static class SharedParameter extends Parameter {
        static final List<Class<?>> TYPES;
        static {
            List<Class<?>> types = new ArrayList<>(Arrays.asList(TableNameDecider.class));
            TYPES = Collections.unmodifiableList(types);
        }

        private final @Nullable DeciderParam deciderParamAnnotation;
        private final Optional<String> name;
        private final boolean isTableNameDecider;

        protected SharedParameter(MethodParameter parameter) {
            super(parameter);
            this.deciderParamAnnotation = parameter.getParameterAnnotation(DeciderParam.class);
            name = getDeciderParamName();
            isTableNameDecider = isTableNameDeciderType(parameter.getParameterType());
        }

        public boolean isTableNameDeciderParameter() {
            return isTableNameDecider;
        }

        @Override
        public boolean isSpecialParameter() {
            if( isDeciderParamParameter() ) {
                return !isDeciderParamBindable();
            }
            return super.isSpecialParameter() || isTableNameDeciderParameter();
        }

        @Override
        public boolean isNamedParameter() {
            return super.isNamedParameter() || isDeciderParamBindable();
        }

        @Override
        public Optional<String> getName() {
           return name.isPresent()?name:super.getName();
        }

        public boolean isDeciderParamParameter() {
            return this.deciderParamAnnotation != null;
        }

        private boolean isTableNameDeciderType(Class<?> parameterType) {
            for (Class<?> specialParameterType : TYPES) {
                if (specialParameterType.isAssignableFrom(parameterType)) {
                    return true;
                }
            }
            return false;
        }

        private Optional<String> getDeciderParamName() {
            if (!isDeciderParamParameter() || !StringUtils.hasText(this.deciderParamAnnotation.value()))
                return Optional.empty();
            return Optional.of(this.deciderParamAnnotation.value());
        }

        private boolean isDeciderParamBindable() {
            return isDeciderParamParameter() && this.deciderParamAnnotation.bindable();
        }
    }
}
