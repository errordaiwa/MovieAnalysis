package cn.com.sina.like.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

abstract class AbstractRedisClient {
	private static final String MAX_ACTIVE = "config.maxActive";
	private static final String MAX_IDLE = "config.maxIdle";
	private static final String MAX_WAIT = "config.maxWait";
	private static final String TEST_ON_BORROW = "config.testOnBorrow";
	private static final String TEST_ON_RETURN = "config.testOnReturn";
	private static final String SERVER_NUM = "server.count";
	private static final String IP_PERFIX = "server";
	private static final String IP_SUFFIX = ".ip";
	private static final String PORT_PERFIX = "server";
	private static final String PORT_SUFFIX = ".port";

	protected ShardedJedisPool pool;

	protected AbstractRedisClient(String configName) {
		ResourceBundle bundle = ResourceBundle.getBundle(configName);
		if (bundle == null) {
			throw new IllegalArgumentException(
					"[redis.properties] is not found!");
		}
		JedisPoolConfig config = new JedisPoolConfig();
		// 从redis.propertie中读取Redis pool的配置
		config.setMaxActive(Integer.valueOf(bundle.getString(MAX_ACTIVE)));
		config.setMaxIdle(Integer.valueOf(bundle.getString(MAX_IDLE)));
		config.setMaxWait(Long.valueOf(bundle.getString(MAX_WAIT)));
		config.setTestOnBorrow(Boolean.valueOf(bundle.getString(TEST_ON_BORROW)));
		config.setTestOnReturn(Boolean.valueOf(bundle.getString(TEST_ON_RETURN)));
		int serverNum = Integer.parseInt(bundle.getString(SERVER_NUM));
		List<JedisShardInfo> list = new LinkedList<JedisShardInfo>();
		for (int i = 1; i <= serverNum; i++) {
			JedisShardInfo jedisShardInfo = new JedisShardInfo(
					bundle.getString(IP_PERFIX + i + IP_SUFFIX),
					Integer.valueOf(bundle.getString(PORT_PERFIX + i
							+ PORT_SUFFIX)));
			list.add(jedisShardInfo);
		}
		pool = new ShardedJedisPool(config, list);
	}

	/*
	 * 向cache中插入key-value键值对
	 */
	public void setValue(String key, String value) {
		ShardedJedis connection = pool.getResource();
		connection.set(key, value);
		pool.returnResource(connection);
	}

	/*
	 * 从cache中获取key对应的单个value
	 */
	public String getValue(String key) {
		ShardedJedis connection = pool.getResource();
		String valueString = connection.get(key);
		pool.returnResource(connection);
		return valueString;
	}

	/*
	 * 从cache中移除key-value键值对
	 */
	public long deleteValue(String key) {
		ShardedJedis connection = pool.getResource();
		long result = connection.del(key);
		pool.returnResource(connection);
		return result;
	}

	/*
	 * 向cache中插入key-valueList键值对
	 */
	public void setList(String key, String[] values) {
		ShardedJedis connection = pool.getResource();
		connection.lpush(key, values);
		pool.returnResource(connection);
	}

	/*
	 * 从cache中获取key对应的list
	 */
	public List<String> getList(String key) {
		ShardedJedis connection = pool.getResource();
		List<String> list = connection.lrange(key, 0, -1);
		pool.returnResource(connection);
		return list;
	}

	/*
	 * 若List存在，则操作成功返回true；反之则返回false
	 */
	public boolean addToList(String key, String value) {
		ShardedJedis connection = pool.getResource();
		long result = connection.lpushx(key, value);
		pool.returnResource(connection);
		return result != 0L;
	}
	
//	/*
//	 * 若Set存在，则操作成功返回true；反之则返回false
//	 */
//	public boolean addToSet(String key, double score, String member) {
//		ShardedJedis connection = pool.getResource();
//		long result = connection.zadd(key, score, member);
//		pool.returnResource(connection);
//		return result != 0L;
//	}

	/*
	 * 返回删除的元素个数，若List中不含该元素或List不存在，则返回0
	 */
	public long deleteFromList(String key, String value) {
		ShardedJedis connection = pool.getResource();
		long result = connection.lrem(key, 0, value);
		pool.returnResource(connection);
		return result;
	}

	/*
	 * 检查传入的key是否已经存在于cache中
	 */
	public boolean exists(String key) {
		ShardedJedis connection = pool.getResource();
		boolean isInCache = connection.exists(key);
		pool.returnResource(connection);
		return isInCache;
	}

	/*
	 * 查询该key在cache中对应的list的长度
	 */
	public long getListLen(String key) {
		ShardedJedis connection = pool.getResource();
		long length = connection.llen(key);
		pool.returnResource(connection);
		return length;
	}
}
