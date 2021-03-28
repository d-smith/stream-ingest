package ds.streamingest.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ds.streamingest.model.PartitionKeyExtractorDescription;

import org.springframework.stereotype.Repository;

public class PartitionKeyExtractorDescRepo implements ObjectRepository<PartitionKeyExtractorDescription> {

    private Map<String, PartitionKeyExtractorDescription> repository;

	public PartitionKeyExtractorDescRepo() {
		this.repository = new HashMap<>();
	}

	@Override
	public void store(PartitionKeyExtractorDescription d) {
		repository.put(d.getStreamName(), d);
	}

	@Override
	public PartitionKeyExtractorDescription retrieve(String id) {
		return repository.get(id);
	}

	@Override
	public PartitionKeyExtractorDescription search(String streamName) {
		Collection<PartitionKeyExtractorDescription> descs = repository.values();
		for (PartitionKeyExtractorDescription d : descs) {
			if (d.getStreamName().equalsIgnoreCase(streamName))
				return d;
		}
		return null;
	}

	@Override
	public PartitionKeyExtractorDescription delete(String id) {
		PartitionKeyExtractorDescription d = repository.get(id);
		this.repository.remove(id);
		return d;
	}
}