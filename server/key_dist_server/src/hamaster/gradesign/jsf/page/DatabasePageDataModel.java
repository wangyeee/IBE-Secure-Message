package hamaster.gradesign.jsf.page;

import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;

/**
 * 完成Richfaces调用数据库分页的类
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 * @param <E> 显示的类模板
 */
public abstract class DatabasePageDataModel<E> extends DataModel<E> {

	/**
	 * 每一页大小
	 */
	private int pageSize;

	/**
	 * cache中第一条数据相对于数据库中第一条数据的偏移量
	 */
	private int absoluteOffset;

	/**
	 * 要获取的数据相对于数据库中第一条数据的偏移量
	 */
	private int index;

	/**
	 * 内存中的一页数据
	 */
	private List<E> cache;

	/**
	 * 缓存的数据总数
	 */
	protected volatile int count;

	protected long interval = 30 * 1000L;

	private volatile boolean running;

	/**
	 * @param pageSize 每一页显示数量
	 */
	public DatabasePageDataModel(int pageSize) {
		this.pageSize = pageSize;
		absoluteOffset = 0;
		index = 0;
		count = -1;
		running = true;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	@Override
	public E getRowData() {
		if (!isRowAvailable())
			return null;
		if (index < absoluteOffset || index > absoluteOffset + pageSize || cache == null) {
			int page = index / pageSize;
			cache = list(page, pageSize);
			absoluteOffset = page * pageSize;
		}
		return cache.get(index - absoluteOffset);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	@Override
	public boolean isRowAvailable() {
		return getRowIndex() < getRowCount() && getRowIndex() != -1 && getRowCount() != -1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		int dbCount = getCountImpl();
		return dbCount > 0 ? dbCount : -1;
	}


	public int getCount() {
		if (count > -1)
			return count;
		count = getCountImpl();
		// Richfaces会频繁调用getCountImpl方法，一个简单的缓存提高效率
		Thread t = new Thread() {
			@Override
			public void run() {
				while (DatabasePageDataModel.this.running) {
					try {
						Thread.sleep(getInterval());
					} catch (InterruptedException e) {
					}
					DatabasePageDataModel.this.count = getCount();
				}
			}
		};
		t.start();
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowIndex()
	 */
	@Override
	public int getRowIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#setRowIndex(int)
	 */
	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex < -1) {
			throw new IllegalArgumentException();
		}
		int old = index;
		index = rowIndex;
		if (cache == null) {
			return;
		}
		DataModelListener[] listeners = getDataModelListeners();
		if ((old != index) && (listeners != null)) {
			Object rowData = null;
			if (isRowAvailable()) {
				rowData = getRowData();
			}
			DataModelEvent event = new DataModelEvent(this, index, rowData);
			int n = listeners.length;
			for (int i = 0; i < n; i++) {
				if (null != listeners[i]) {
					listeners[i].rowSelected(event);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	@Override
	public Object getWrappedData() {
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setWrappedData(Object data) {
		if (data == null) {
			cache = null;
			setRowIndex(-1);
			return;
		}
		if (data instanceof List) {
			cache = (List<E>) data;
			index = -1;
			setRowIndex(0);
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getAbsoluteOffset() {
		return absoluteOffset;
	}

	public void setAbsoluteOffset(int absoluteOffset) {
		this.absoluteOffset = absoluteOffset;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void stop() {
		this.running = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		this.running = false;
		super.finalize();
	}

	/**
	 * 分页获取数据
	 * @param page 页码 从0开始
	 * @param pageSize 每一页数量
	 * @return 数据列表
	 */
	public abstract List<E> list(int page, int pageSize);
	
	/**
	 * @return 数据库中的记录数量
	 */
	public abstract int getCountImpl();
}
