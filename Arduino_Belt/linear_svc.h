#ifndef LINEAR_SVC_H
#define LINEAR_SVC_H

#include <vector>
using namespace std;

class LinearSVC {
private:
    const int feature_dim = 10;
    const vector<float> weights = 
{
  -0.33892589726235023, 0.024613149584438408, -0.21891464756368814, 0.1687266148792491, -0.02367180337462008, 0.006899706017671354, -0.020285664678372405, 0.5050338560670466, -0.00863544722715825, -0.011461456551231774
};


    const float bias = 0.007686918758498276;

    float dotProduct(const vector<float>& x) const {
        float sum = 0.0;
        for (int i = 0; i < feature_dim; i++) {
            sum += x[i] * weights[i];
        }
        return sum;
    }

public:
    int predict(const vector<float>& x) const {
        float decision = dotProduct(x) + bias;
        return (decision >= 0) ? 1 : -1;
    }
};

#endif
