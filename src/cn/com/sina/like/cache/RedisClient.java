package cn.com.sina.like.cache;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.ShardedJedis;

public class RedisClient extends AbstractRedisClient {

	protected RedisClient(String configName) {
		super(configName);
	}

	public void setValueLong(String key, String value) {
		setValue(key, value);
	}

	public long getValueLong(String key) {
		return Long.parseLong(getValue(key));
	}

	public void setListLong(String key, List<Long> list) {
		ShardedJedis connection = pool.getResource();
		if (list != null) {
			connection.del(key);
			String[] values = new String[list.size() + 1];
			// -1L作为哨兵值存在，表示该key已经存在于cache中
			values[0] = "-1";
			for (int i = 0; i < list.size(); i++) {
				values[i + 1] = Long.toString(list.get(i));
			}
			connection.lpush(key, values);
			if (connection.llen(key) != list.size() + 1) {
				connection.del(key);
			}
		}

		pool.returnResource(connection);
	}

	public boolean addLongToList(String key, long value) {
		return addToList(key, Long.toString(value));
	}
	

	public long deleteLongFromList(String key, long value) {
		return deleteFromList(key, Long.toString(value));
	}

	public ArrayList<Long> getListLong(String key) {
		ArrayList<Long> list = new ArrayList<Long>();
		List<String> stringList = getList(key);
		for (String stringValue : stringList) {
			list.add(Long.parseLong(stringValue));
		}
		return list;
	}
}
