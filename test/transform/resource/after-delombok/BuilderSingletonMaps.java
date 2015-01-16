import java.util.Map;
import java.util.SortedMap;
class BuilderSingletonMaps<K, V> {
	private Map<K, V> women;
	private SortedMap<K, ? extends Number> men;
	@SuppressWarnings("all")
	private Map rawMap;
	private Map<String, V> stringMap;
	@java.lang.SuppressWarnings("all")
	BuilderSingletonMaps(final Map<K, V> women, final SortedMap<K, ? extends Number> men, final Map rawMap, final Map<String, V> stringMap) {
		this.women = women;
		this.men = men;
		this.rawMap = rawMap;
		this.stringMap = stringMap;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingletonMapsBuilder<K, V> {
		private java.util.ArrayList<K> women$key;
		private java.util.ArrayList<V> women$value;
		private java.util.ArrayList<K> men$key;
		private java.util.ArrayList<Number> men$value;
		private java.util.ArrayList<java.lang.Object> rawMap$key;
		private java.util.ArrayList<java.lang.Object> rawMap$value;
		private java.util.ArrayList<String> stringMap$key;
		private java.util.ArrayList<V> stringMap$value;
		@java.lang.SuppressWarnings("all")
		BuilderSingletonMapsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> woman(final K womanKey, final V womanValue) {
			if (this.women$key == null) {
				this.women$key = new java.util.ArrayList<K>();
				this.women$value = new java.util.ArrayList<V>();
			}
			this.women$key.add(womanKey);
			this.women$value.add(womanValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> women(final java.util.Map<? extends K, ? extends V> women) {
			if (this.women$key == null) {
				this.women$key = new java.util.ArrayList<K>();
				this.women$value = new java.util.ArrayList<V>();
			}
			for (final java.util.Map.Entry<? extends K, ? extends V> $lombokEntry : women.entrySet()) {
				this.women$key.add($lombokEntry.getKey());
				this.women$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> man(final K manKey, final Number manValue) {
			if (this.men$key == null) {
				this.men$key = new java.util.ArrayList<K>();
				this.men$value = new java.util.ArrayList<Number>();
			}
			this.men$key.add(manKey);
			this.men$value.add(manValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> men(final java.util.Map<? extends K, ? extends Number> men) {
			if (this.men$key == null) {
				this.men$key = new java.util.ArrayList<K>();
				this.men$value = new java.util.ArrayList<Number>();
			}
			for (final java.util.Map.Entry<? extends K, ? extends Number> $lombokEntry : men.entrySet()) {
				this.men$key.add($lombokEntry.getKey());
				this.men$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> rawMap(final java.lang.Object rawMapKey, final java.lang.Object rawMapValue) {
			if (this.rawMap$key == null) {
				this.rawMap$key = new java.util.ArrayList<java.lang.Object>();
				this.rawMap$value = new java.util.ArrayList<java.lang.Object>();
			}
			this.rawMap$key.add(rawMapKey);
			this.rawMap$value.add(rawMapValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> rawMap(final java.util.Map<?, ?> rawMap) {
			if (this.rawMap$key == null) {
				this.rawMap$key = new java.util.ArrayList<java.lang.Object>();
				this.rawMap$value = new java.util.ArrayList<java.lang.Object>();
			}
			for (final java.util.Map.Entry<?, ?> $lombokEntry : rawMap.entrySet()) {
				this.rawMap$key.add($lombokEntry.getKey());
				this.rawMap$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> stringMap(final String stringMapKey, final V stringMapValue) {
			if (this.stringMap$key == null) {
				this.stringMap$key = new java.util.ArrayList<String>();
				this.stringMap$value = new java.util.ArrayList<V>();
			}
			this.stringMap$key.add(stringMapKey);
			this.stringMap$value.add(stringMapValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMapsBuilder<K, V> stringMap(final java.util.Map<? extends String, ? extends V> stringMap) {
			if (this.stringMap$key == null) {
				this.stringMap$key = new java.util.ArrayList<String>();
				this.stringMap$value = new java.util.ArrayList<V>();
			}
			for (final java.util.Map.Entry<? extends String, ? extends V> $lombokEntry : stringMap.entrySet()) {
				this.stringMap$key.add($lombokEntry.getKey());
				this.stringMap$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonMaps<K, V> build() {
			java.util.Map<K, V> women;
			switch (this.women$key == null ? 0 : this.women$key.size()) {
			case 0:
				women = java.util.Collections.emptyMap();
				break;
			case 1:
				women = java.util.Collections.singletonMap(this.women$key.get(0), this.women$value.get(0));
				break;
			default:
				women = new java.util.LinkedHashMap<K, V>(this.women$key.size() < 1073741824 ? 1 + this.women$key.size() + (this.women$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.women$key.size(); $i++) women.put(this.women$key.get($i), this.women$value.get($i));
				women = java.util.Collections.unmodifiableMap(women);
			}
			java.util.SortedMap<K, Number> men = new java.util.TreeMap<K, Number>();
			if (this.men$key != null) for (int $i = 0; $i < (this.men$key == null ? 0 : this.men$key.size()); $i++) men.put(this.men$key.get($i), this.men$value.get($i));
			men = java.util.Collections.unmodifiableSortedMap(men);
			java.util.Map<java.lang.Object, java.lang.Object> rawMap;
			switch (this.rawMap$key == null ? 0 : this.rawMap$key.size()) {
			case 0:
				rawMap = java.util.Collections.emptyMap();
				break;
			case 1:
				rawMap = java.util.Collections.singletonMap(this.rawMap$key.get(0), this.rawMap$value.get(0));
				break;
			default:
				rawMap = new java.util.LinkedHashMap<java.lang.Object, java.lang.Object>(this.rawMap$key.size() < 1073741824 ? 1 + this.rawMap$key.size() + (this.rawMap$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.rawMap$key.size(); $i++) rawMap.put(this.rawMap$key.get($i), this.rawMap$value.get($i));
				rawMap = java.util.Collections.unmodifiableMap(rawMap);
			}
			java.util.Map<String, V> stringMap;
			switch (this.stringMap$key == null ? 0 : this.stringMap$key.size()) {
			case 0:
				stringMap = java.util.Collections.emptyMap();
				break;
			case 1:
				stringMap = java.util.Collections.singletonMap(this.stringMap$key.get(0), this.stringMap$value.get(0));
				break;
			default:
				stringMap = new java.util.LinkedHashMap<String, V>(this.stringMap$key.size() < 1073741824 ? 1 + this.stringMap$key.size() + (this.stringMap$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.stringMap$key.size(); $i++) stringMap.put(this.stringMap$key.get($i), this.stringMap$value.get($i));
				stringMap = java.util.Collections.unmodifiableMap(stringMap);
			}
			return new BuilderSingletonMaps<K, V>(women, men, rawMap, stringMap);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingletonMaps.BuilderSingletonMapsBuilder(women$key=" + this.women$key + ", women$value=" + this.women$value + ", men$key=" + this.men$key + ", men$value=" + this.men$value + ", rawMap$key=" + this.rawMap$key + ", rawMap$value=" + this.rawMap$value + ", stringMap$key=" + this.stringMap$key + ", stringMap$value=" + this.stringMap$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <K, V> BuilderSingletonMapsBuilder<K, V> builder() {
		return new BuilderSingletonMapsBuilder<K, V>();
	}
}
