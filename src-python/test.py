from de.jreality.plugin import JRViewerUtility
from de.jreality.geometry import Primitives
from de.jtem.halfedgetools.plugin import HalfedgeInterface

content = JRViewerUtility.getContentPlugin(C);
content.setContent(Primitives.torus(0.4, 0.2, 30, 20));

H = C.getPlugin(HalfedgeInterface);

hds = H.get();
H.clearSelection();

s = H.getSelection();
for i in range(0, 150):
    s.setSelected(hds.getFace(i), True);
H.setSelection(s);

delAlgo = H.getAlgorithm("Remove Face");
delAlgo.execute();