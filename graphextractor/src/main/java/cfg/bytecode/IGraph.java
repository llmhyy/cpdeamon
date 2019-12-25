package cfg.bytecode;

import java.util.List;

public interface IGraph<T extends IGraphNode<T>> {

	List<T> getNodeList();

	List<T> getExitList();
	
}
