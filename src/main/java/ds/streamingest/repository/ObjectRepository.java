package ds.streamingest.repository;

public interface ObjectRepository<T> {
    public void store(T t);
    public T retrieve(String id);
    public T search(String id);
    public T delete(String id);
}