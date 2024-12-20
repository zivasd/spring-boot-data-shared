package io.github.zivasd.spring.boot.data.shared.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface TableNameDecider {
	List<String> decideNames(Map<String, Object> paramMap);

	public static class NoOperator implements TableNameDecider {
		public static final TableNameDecider INSTANCE = new NoOperator();

		@Override
		public List<String> decideNames(Map<String, Object> paramMap) {
			return Collections.emptyList();
		}
	}
}
