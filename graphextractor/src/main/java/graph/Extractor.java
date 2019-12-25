package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import cfg.bytecode.ByteCodeMethodFinder;
import cfg.bytecode.CFG;
import cfg.bytecode.CFGConstructor;
import cfg.bytecode.CFGNode;
import cfg.bytecode.MethodFinderByLine;
import cfg.cfgcoverage.CFGInstance;
import cfg.model.ClassLocation;
import cfg.utils.ClassUtils;
import cfg.utils.InstrumentationUtils;
import cfg.utils.SignatureUtils;

public class Extractor {
	public CFGInstance findCfg(ClassLocation methodLocation, String file_name) {
		String methodId = InstrumentationUtils.getMethodId(methodLocation.getClassCanonicalName(),
				methodLocation.getMethodSign());
		ByteCodeMethodFinder finder;
		if (methodLocation.getLineNumber() >= 0) {
			finder = new MethodFinderByLine(methodLocation);
		} else {
			finder = new MethodFinderByMethodSignature(methodLocation);
		}
		parse(finder,file_name);
		Method method = finder.getMethod();
		if (method == null || method.isAbstract() || (method.getCode() == null)) {
			return new CFGInstance(null, methodId, Collections.<CFGNode>emptyList());
		}
		CFGConstructor cfgConstructor = new CFGConstructor();
		CFG cfg = cfgConstructor.constructCFG(method.getCode());
		cfg.setMethod(method);
		List<CFGNode> nodeList = new ArrayList<CFGNode>(cfg.getNodeList());
		Collections.sort(nodeList, new Comparator<CFGNode>() {

			@Override
			public int compare(CFGNode o1, CFGNode o2) {
				return Integer.compare(o1.getIdx(), o2.getIdx());
			}

		});
		/* fill up line number info */
		for (CFGNode node : nodeList) {
			node.setLineNo(method.getLineNumberTable().getSourceLine(node.getInstructionHandle().getPosition()));
		}

		methodId = InstrumentationUtils.getMethodId(methodLocation.getClassCanonicalName(), method);
		return new CFGInstance(cfg, methodId, nodeList);
	}

	private static class MethodFinderByMethodSignature extends ByteCodeMethodFinder {
		private List<String> methodNames;
		private String methodSign;

		public MethodFinderByMethodSignature(ClassLocation loc) {
			String methodName = SignatureUtils.extractMethodName(loc.getMethodSign());
			if (methodName.equals(ClassUtils.getSimpleName(loc.getClassCanonicalName()))) {
				this.methodNames = Arrays.asList("<init>", "<clinit>");
			} else {
				this.methodNames = Arrays.asList(methodName);
			}
			this.methodSign = SignatureUtils.extractSignature(loc.getMethodSign());
		}

		public void visitMethod(Method method) {
			if (this.methodNames.contains(method.getName()) && this.methodSign.equals(method.getSignature())) {
				setMethod(method);
			}
		}
	}

	public static void parse(ByteCodeMethodFinder visitor, String classPath) {
		try {
			ClassParser cp = new ClassParser(classPath);
			JavaClass clazz = cp.parse();
			clazz.accept(new DescendingVisitor(clazz, visitor));
			visitor.setJavaClass(clazz);
			Method[] methods=clazz.getMethods();
			visitor.setMethod(methods[1]);
		} catch (Exception e) {
		  e.printStackTrace();
		}

	}
}
