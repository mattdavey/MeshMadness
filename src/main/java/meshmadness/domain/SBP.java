package meshmadness.domain;

import meshmadness.actors.SalesPerson;
import meshmadness.actors.User;
import meshmadness.messaging.LocalPayload;
import meshmadness.messaging.MeshPayload;
import meshmadness.messaging.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SBP implements Runnable {
    final Logger logger = LoggerFactory.getLogger(SBP.class);

    final private String name;
    final private List<SBP> mesh = new ArrayList<>();
    final private List<User> users = new ArrayList<>();
    final private List<SalesPerson> dealers = new ArrayList<>();
    final private BlockingQueue<Payload> rfqQueue = new LinkedBlockingQueue<>();
    final private Map<Integer, RFQStateManager> workingRFQs = new HashMap<>();

    public class RFQSubjectHolder {
        public final int id;
        public final RFQStateManager.RFQState state;
        public final String fillerName;

        public RFQSubjectHolder(final int rfqId, final RFQStateManager.RFQState state, final String fillerName) {
            this.id = rfqId;
            this.state = state;
            this.fillerName = fillerName;
        }
    }

    private ReplaySubject<RFQSubjectHolder> subjectRFQSubjectHolder = ReplaySubject.create();
    public Observable<RFQSubjectHolder> subscribe() {
        return subjectRFQSubjectHolder;
    }

    public SBP(String name) {
        this.name = name;
    }

    public void logon(final User user) {
        assert this.users.contains(user);
        users.add(user);
    }

    public void join(final SBP sbp) {
        mesh.add(sbp);
    }

    public int getWorkingRFQCount() {
        return workingRFQs.size();
    }

    public void logon(final SalesPerson salesPerson) {
        assert this.dealers.contains(salesPerson);
        this.dealers.add(salesPerson);
    }

    public void clientIncomingCommunication(final RFQ rfq) {
        if (!workingRFQs.containsKey(rfq.getRFQId())) {

            final RFQStateManager rfqStateManager = new RFQStateManager(this, rfq);
            notifyAllMesh(rfq, rfqStateManager.getCurrentState(), rfqStateManager.getCurrentStateTime());
            workingRFQs.put(rfq.getRFQId(), rfqStateManager);
        }

        rfqQueue.add(rfq);
    }

    public void notifyLocalSales(final RFQ rfq, final RFQStateManager.RFQState state) {
        logger.debug(String.format("%d (%s) Notify all sales people RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
        for (final SalesPerson salesPerson : getSalesPersons()) {
            salesPerson.SalesPersonCommunication(rfq, state);
        }
    }

    public void run() {
        while(true){
            try {
                final Payload rfq = rfqQueue.take();
                // Find RFQ manager
                final RFQStateManager rfqStateManager = workingRFQs.get(rfq.getRFQId());
                if (rfqStateManager != null) {
                    rfqStateManager.NextState(rfq);
                    subjectRFQSubjectHolder.onNext(new RFQSubjectHolder(rfq.getRFQId(), rfqStateManager.getCurrentState(), rfqStateManager.getQuoterName()));
                } else {
                    new RuntimeException("Unregistered RFQ id");
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }

    synchronized public void MeshCommunications(final MeshPayload meshPayload) {
        logger.debug(String.format("%d (%s) Received from Mesh RFQ%s (%s) from %s (%s)", System.nanoTime(), name, meshPayload.getRFQId(), meshPayload.getState(), meshPayload.getSource().getName(), meshPayload.getTime()));

        // If we have never seen the RFQ before, add it to the list to work on
        if (!workingRFQs.containsKey(meshPayload.getRFQ().getRFQId())) {
            final RFQStateManager rfqStateManager = new RFQStateManager(this, meshPayload.getRFQ());
            workingRFQs.put(meshPayload.getRFQ().getRFQId(), rfqStateManager);
            logger.debug(String.format("%d (%s) Creating NEW RFQStateManager (%s,StartRFQ,RFQ%s)", System.nanoTime(), name, meshPayload.getRFQ().getUsername(), meshPayload.getRFQ().getRFQId()));

            notifyAllMesh(meshPayload.getRFQ(), rfqStateManager.getCurrentState(), rfqStateManager.getCurrentStateTime());
        }

        // Process Mesh message
        rfqQueue.add(meshPayload);
    }

    public List<SalesPerson> getSalesPersons() {
        return dealers;
    }

    public void send(final LocalPayload salesPersonModification) {
        rfqQueue.add(salesPersonModification);
    }

    public String getName() {
        return name;
    }

    synchronized public void notifyAllMesh(final RFQ rfq, final RFQStateManager.RFQState state, final long time) {
        for (final SBP region : mesh) {
            try {
                if (region != rfq.getOriginatingSBP()) {
                    region.MeshCommunications(new MeshPayload((RFQ) rfq.clone(), state, this, time));
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void notifyOneRegion(final SBP source, final RFQ rfq, final RFQStateManager.RFQState state, long time) {
        source.MeshCommunications(new MeshPayload(rfq, state, this, time));
    }
}
