#ifndef MINMAX_SCALER_H
#define MINMAX_SCALER_H

#include <vector>
#include <algorithm>

using namespace std;

class MinMaxScaler {
private:
    vector<float> min_vals = {
0.7632878443505657, 0.85, 0.0, -4.45, 0.0, -1.583288980609728, -7.925000000000001, 0.0351662717197411, -1.92760180995475, -19.308};





    vector<float> max_vals = {
14.879454963136247, 59.89, 10.48, 11.73, 23.0, 43.41539100781793, 10.51, 13.08202475409165, 55.0169491525424, 47.97409999999999};


    float range_min = -1.0;
    float range_max = 1.0;

    
public:
    vector<float> transform(const vector<float>& features) const {
        vector<float> scaled;
        scaled.reserve(features.size());

        for (size_t i = 0; i < features.size(); i++) {
            float min_val = min_vals[i];
            float max_val = max_vals[i];
            float val = features[i];

            float scaled_val = (max_val - min_val) != 0
                ? ((val - min_val) / (max_val - min_val)) * (range_max - range_min) + range_min
                : range_min;

            scaled.push_back(scaled_val);
        }

        return scaled;
    }
};

#endif
