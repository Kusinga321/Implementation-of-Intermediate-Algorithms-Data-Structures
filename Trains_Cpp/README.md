# Trains

A header-only C++20 library for assembling passenger trains. A dispatcher takes the next locomotive from a depot and adds wagons of the requested series until it reaches the requested count, runs out of matching wagons, or reaches the locomotive's hauling capacity.

Each wagon has a serial number. Copy construction gives the new wagon its own number, while moving transfers the existing number. The example program in `trains_example.cpp` checks these rules and demonstrates how to assemble a train and query its seat count.

From this directory, build and run the example with

```sh
c++ -std=c++20 trains_example.cpp -o trains_example
./trains_example
```

[Original assignment](https://docs.google.com/document/d/1_35wRGWWY2VnFLEiIqjAL25S7MWb5MPMgvql7BSyFQ8/edit?tab=t.0)
