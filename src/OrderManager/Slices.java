package OrderManager;
/*
import java.util.ArrayList;

public class Slices {

    public double initialMarketPrice;
    ArrayList<Order> slices;
    ArrayList<Fill>fills;
    private Order order;

    public Slices(ArrayList<Order> slices, ArrayList<Fill> fills, double initialMarketPrice) {
        this.slices = new ArrayList<Order>();
        this.fills = new ArrayList<Fill>();
        this.initialMarketPrice = initialMarketPrice;
    }

    void cross(Order matchingOrder){

        order.getClientOrderID();
        // pair slices first and then parent
        for(Order slice:slices){ // looping through every slice in current order
            if(slice.sizeRemaining() == 0) continue; // no more orders
            //TODO could optimise this to not start at the beginning every time
            for(Order matchingSlice:matchingOrder.slices){ // take slice from argument slices and compare it to the slices in current slice
                int msze=matchingSlice.sizeRemaining(); // matchingSlice number of orders remaining
                if(msze == 0) continue;
                int size = slice.sizeRemaining();
                if(size <= msze){
                    slice.createFill(size,initialMarketPrice);
                    matchingSlice.createFill(size, initialMarketPrice);
                    break;
                }
                //size>msze
                slice.createFill(msze,initialMarketPrice);
                matchingSlice.createFill(msze, initialMarketPrice);
            }
            int size=slice.sizeRemaining();
            int mParent=matchingOrder.sizeRemaining()-matchingOrder.sliceSizes();
            if(size > 0 && mParent > 0){
                if(size >= mParent){
                    slice.createFill(size,initialMarketPrice);
                    matchingOrder.createFill(size, initialMarketPrice);
                }else{
                    slice.createFill(mParent,initialMarketPrice);
                    matchingOrder.createFill(mParent, initialMarketPrice);
                }
            }
            //no point continuing if we didn't fill this slice, as we must already have fully filled the matchingOrder
            if(slice.sizeRemaining() > 0)break;
        }
    }

    public int sliceSizes(){ // change c.size to c.getSize: make size private, add size getter
        int totalSizeOfSlices=0;

        for(Order c:slices)totalSizeOfSlices += c.size;

        return totalSizeOfSlices;
    }
    public int newSlice(int sliceSize){
        slices.add(new Order(order.id, order.getClientOrderID(),order.instrument,sliceSize));
        return slices.size()-1;
    }
    public int sizeFilled(){
        int filledSoFar=0;

        for(Fill f:fills){
            filledSoFar += f.size; // f.getSize();
        }
        for(Order c:slices){
            filledSoFar += c.sizeFilled();
        }

        return filledSoFar;
    }
    public int sizeRemaining(){
        return order.size-sizeFilled();
    }

//    float price(int accountNumberOfSlice){
//        float sum = 0;
//        // get the slice by its clientOrderId
//        for(Order slice: slices) {
//            if(slice.getClientOrderID() == accountNumberOfSlice) {
//                // sum all the fills of that slice
//                for(Fill fill : slice.fills){
//                    sum+=fill.price; // change to fill.getPrice()
//                }
//            }
//        }
//
//        return sum/fills.size();
//    }
    void createFill(int size,double price){
        fills.add(new Fill(size,price));

        if(sizeRemaining() == 0){
            order.OrdStatus = '2';
        } else{
            order.OrdStatus = '1';
        }
    }
}
*/