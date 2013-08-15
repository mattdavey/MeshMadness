package meshmadness;

import meshmadness.actors.SalesPerson;
import meshmadness.actors.User;
import meshmadness.messaging.LocalPayload;
import meshmadness.messaging.MeshPayload;
import meshmadness.messaging.Payload;
import rx.Observable;
import rx.subjects.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SBP implements Runnable {
    final private String name;
    final private List<SBP> mesh = new ArrayList<SBP>();
    final private List<User> users = new ArrayList<User>();
    final private List<SalesPerson> salesPerson = new ArrayList<SalesPerson>();
    final private BlockingQueue<Payload> rfqQueue = new LinkedBlockingQueue<Payload>();
    final private Map<Integer, RFQStateManager> workingRFQs = new HashMap<Integer, RFQStateManager>();

    public class RFQSubjectHolder {
        public final int id;
        public final RFQStateManager.RFQState state;

        public RFQSubjectHolder(final int rfqId, final RFQStateManager.RFQState state) {
            this.id = rfqId;
            this.state = state;
        }
    }
    private ReplaySubject<RFQSubjectHolder> subjectRFQSubjectHolder = ReplaySubject.create();
    public Observable<RFQSubjectHolder> subscribe() {
        return subjectRFQSubjectHolder;
    }

    public SBP(String name) {
        this.name = name;
    }

    public void login(final User user) {
        users.add(user);
    }

    public void join(final SBP sbp) {
        mesh.add(sbp);
    }

    public int getWorkingRFQCount() {
        return workingRFQs.size();
    }

    public void registerSalesPerson(final SalesPerson salesPerson) {
        assert this.salesPerson.contains(salesPerson);
        this.salesPerson.add(salesPerson);
    }

    public void clientIncomingCommunication(final RFQ rfq) {
        if (!workingRFQs.containsKey(rfq.getRFQId())) {

            final RFQStateManager rfqStateManager = new RFQStateManager(this, rfq);
            notifyMesh(rfq, rfqStateManager.getCurrentState(), rfqStateManager.getCurrentStateTime());
            workingRFQs.put(rfq.getRFQId(), rfqStateManager);
        }

        rfqQueue.add(rfq);
    }

    public void sendToAllSales(final RFQ rfq, final RFQStateManager.RFQState state) {
        System.out.println(String.format("%d (%s) Notify all sales people RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
        for (final SalesPerson salesPerson : getSalesPersons()) {
            salesPerson.SalesPersonCommunication(rfq, state);
        }
    }

    public void run() {
        while(true){
            try {
                final Payload rfq = rfqQueue.take();
                // Find manager
                final RFQStateManager rfqStateManager = workingRFQs.get(rfq.getRFQId());
                if (rfqStateManager != null) {
                    rfqStateManager.NextState(rfq);
                    subjectRFQSubjectHolder.onNext(new RFQSubjectHolder(rfq.getRFQId(), rfqStateManager.getCurrentState()));
                } else {
                    new RuntimeException("Unregistered RFQ id");
                }
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }

    public void MeshCommunications(final MeshPayload meshPayload) {
        System.out.println(String.format("%d (%s) Received from Mesh: RFQ%s %s from %s %s", System.nanoTime(), name, meshPayload.getRFQId(), meshPayload.getState(), meshPayload.getSource().getName(), meshPayload.getTime()));

        if (!workingRFQs.containsKey(meshPayload.getRFQ().getRFQId())) {
            System.out.println(String.format("%d (%s) Adding to RFQManager: %s StartRFQ RFQ%s", System.nanoTime(), name, meshPayload.getRFQ().getUsername(), meshPayload.getRFQ().getRFQId()));

            final RFQStateManager rfqStateManager = new RFQStateManager(this, meshPayload.getRFQ());
            workingRFQs.put(meshPayload.getRFQ().getRFQId(), rfqStateManager);
            notifyMesh(meshPayload.getRFQ(), rfqStateManager.getCurrentState(), rfqStateManager.getCurrentStateTime());
        }

        rfqQueue.add(meshPayload);
    }

    public List<SalesPerson> getSalesPersons() {
        return salesPerson;
    }

    public void send(LocalPayload salesPersonModification) {
        rfqQueue.add(salesPersonModification);
    }

    public String getName() {
        return name;
    }

    public void notifyMesh(final RFQ rfq, final RFQStateManager.RFQState state, final long time) {
        for (final SBP region : mesh) {
            try {
                if (region != rfq.getSource()) {
                    region.MeshCommunications(new MeshPayload((RFQ) rfq.clone(), state, this, time));
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    public void notfyRegion(final SBP source, final RFQ rfq, final RFQStateManager.RFQState state, long time) {
        source.MeshCommunications(new MeshPayload(rfq, state, this, time));
    }

    public String getRFQState(final int count) {
        return workingRFQs.get(count).getCurrentState().toString();
    }
}
