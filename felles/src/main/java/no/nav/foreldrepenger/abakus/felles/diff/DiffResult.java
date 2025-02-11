package no.nav.foreldrepenger.abakus.felles.diff;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import no.nav.foreldrepenger.abakus.felles.diff.TraverseGraph.TraverseResult;

/**
 * Henter ut resultat fra å diffe to entitet objekter.
 * Det forventes at begge objektene har samme rot.
 */
public class DiffResult {

    private TraverseResult result1;
    private TraverseResult result2;
    @SuppressWarnings("unused")
    private TraverseGraph traverser;

    public DiffResult(TraverseGraph traverser, TraverseResult result1, TraverseResult result2) {
        this.traverser = traverser;
        this.result1 = result1;
        this.result2 = result2;
    }

    public Map<Node, Pair> getLeafDifferences() {
        Map<Node, Pair> diffs = new HashMap<>();

        calcLeafDiffs(diffs, false);

        return diffs.isEmpty() ? Collections.emptyMap() : new TreeMap<>(diffs); // sorter for syns skyld
    }

    private void calcLeafDiffs(Map<Node, Pair> diffs, boolean returnOnFirstDiff) {
        // kan vel lages mer effektivt...
        result1.getValues().forEach((key, value) -> diffs.put(key, new Pair(value, null)));

        result2.getValues().forEach((key, value) -> {
            Object elem1 = diffs.containsKey(key) ? diffs.get(key).element1() : null;
            if (!areEqual(key, elem1, value)) {
                diffs.put(key, new Pair(elem1, value));
                if (returnOnFirstDiff) {
                    return;
                }
            } else {
                diffs.remove(key);
            }
        });
    }

    @SuppressWarnings({"rawtypes"})
    private boolean areEqual(Node key, Object left, Object right) {
        if (Objects.equals(left, right)) {
            return true;
        } else {
            // corner caser som kan være ansett som like.

            // List caser - tolerer at lister kan ha forskjellig rekkefølge
            if (left instanceof List ll && right instanceof List lr) {
                return areEqualListsOutOfOrder(key, ll, lr);
            }
        }

        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean areEqualListsOutOfOrder(Node key, List lhsList, List rhsList) {
        Set lhsSet = new HashSet<>(lhsList);
        Set rhsSet = new HashSet<>(rhsList);

        if (lhsSet.size() != lhsList.size()) {
            throw new IllegalArgumentException(
                "Bad Equals eller duplikater i List.  lhsList har forskjellig størrelse fra lhsSet. Key=\"" + key + "\"\n,\"lhsList\"=" + lhsList
                    + "\n,\"rhsList\"=" + rhsList);
        } else if (rhsSet.size() != rhsList.size()) {
            throw new IllegalArgumentException(
                "Bad Equals eller duplikater i List.  rhsList har forskjellig størrelse fra rhsSet. Key=\"" + key + "\"\n,\"lhsList\"=" + lhsList
                    + "\n,\"rhsList\"=" + rhsList);
        }

        return Objects.equals(lhsSet, rhsSet);
    }

    public boolean isEmpty() {
        Map<Node, Pair> diffs = new HashMap<>();
        calcLeafDiffs(diffs, true);
        return diffs.isEmpty();
    }

    public boolean areDifferent() {
        Map<Node, Pair> diffs = new HashMap<>();
        calcLeafDiffs(diffs, true);
        return !diffs.isEmpty();
    }
}
