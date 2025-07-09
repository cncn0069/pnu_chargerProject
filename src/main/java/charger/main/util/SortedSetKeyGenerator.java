package charger.main.util;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

@EnableCaching
@Component("sortedSetKeyGenerator")
public class SortedSetKeyGenerator implements KeyGenerator{
	@Override
	public Object generate(Object target, Method method, Object... params) {
		// TODO Auto-generated method stub
		if(params.length > 0 && params[0] instanceof Set) {
			Set<?> set = (Set<?>) params[0];
			List<Object> sortedList = new ArrayList<>(set);
			sortedList.sort(Comparator.comparing(Object::toString));
			return sortedList.toString();
			
		}
		return Arrays.deepToString(params);
	}
}
