#ifndef EXTRACT_FEATURES_H
#define EXTRACT_FEATURES_H

#include <cmath>
#include <vector>
#include <algorithm>
#include <numeric>

#define WINDOW_SIZE 60
using namespace std;

void compute_features(const vector<float>& buffer,
                      bool need_mean, bool need_std, bool need_rms,
                      bool need_max_amp, bool need_min_amp,
                      bool need_median, bool need_zero_crossing,
                      bool need_skewness, bool need_kurtosis,
                      bool need_q1, bool need_q3, bool need_autocorr,
                      int buffer_index, vector<float>& features) {
    float mean_val = 0, std_val = 0, rms = 0;
    float skewness = 0, kurt = 0, autocorr = 0;
    float min_amp = fabs(buffer[0]), max_amp = fabs(buffer[0]);
    int zero_crossings = 0;
    vector<float> sorted;
    for (int i = 0, j = buffer_index; i < WINDOW_SIZE; i++, j =(j+1) % WINDOW_SIZE) {
      sorted.push_back(buffer[j]);
    }
    for (int i = 0; i < WINDOW_SIZE; i++) {
        float val = sorted[i];
        mean_val += val;
        std_val += val * val;
        if (need_rms) rms += val * val;

        float abs_val = fabs(val);
        if (abs_val < min_amp) min_amp = abs_val;
        if (abs_val > max_amp) max_amp = abs_val;

        if (i > 0 && ((sorted[i - 1] >= 0 && sorted[i] < 0) || (sorted[i - 1] < 0 && sorted[i] >= 0))) {
            zero_crossings++;
        }
    }

    mean_val /= WINDOW_SIZE;
    std_val = sqrt(std_val / WINDOW_SIZE - mean_val * mean_val);
    if (need_rms) rms = sqrt(rms / WINDOW_SIZE);


    sort(sorted.begin(), sorted.end());
    float median_val = (sorted[WINDOW_SIZE / 2] + sorted[(WINDOW_SIZE - 1) / 2]) / 2.0f;
    float q1 = (sorted[WINDOW_SIZE / 4] + sorted[WINDOW_SIZE / 4 - 1]) / 2.0f;
    float q3 = (sorted[3 * WINDOW_SIZE / 4] + sorted[3 * WINDOW_SIZE / 4 - 1]) / 2.0f;

    if (need_skewness || need_kurtosis) {
        for (int i = 0; i < WINDOW_SIZE; i++) {
            float diff = buffer[i] - mean_val;
            skewness += pow(diff, 3);
            kurt += pow(diff, 4);
        }
        skewness /= WINDOW_SIZE * pow(std_val + 1e-6f, 3);
        kurt = kurt / (WINDOW_SIZE * pow(std_val + 1e-6f, 4)) - 3;
    }

    if (need_autocorr) {
        vector<float> corr(2 * WINDOW_SIZE - 1, 0);
        for (int lag = -WINDOW_SIZE + 1; lag < WINDOW_SIZE; lag++) {
            float sum = 0;
            for (int j = 0; j < WINDOW_SIZE; j++) {
                int k = j + lag;
                if (k >= 0 && k < WINDOW_SIZE) {
                    sum += buffer[j] * buffer[k];
                }
            }
            corr[lag + WINDOW_SIZE - 1] = sum;
        }
        sort(corr.begin(), corr.end());
        autocorr = (corr[(2 * WINDOW_SIZE - 1) / 2] + corr[(2 * WINDOW_SIZE - 1) / 2 - 1]) / 2.0f;
    }

    if (need_mean) features.push_back(mean_val);
    if (need_std) features.push_back(std_val);
    if (need_rms) features.push_back(rms);
    if (need_max_amp) features.push_back(max_amp);
    if (need_min_amp) features.push_back(min_amp);
    if (need_median) features.push_back(median_val);
    if (need_zero_crossing) features.push_back(static_cast<float>(zero_crossings));
    if (need_skewness) features.push_back(skewness);
    if (need_kurtosis) features.push_back(kurt);
    if (need_q1) features.push_back(q1);
    if (need_q3) features.push_back(q3);
    if (need_autocorr) features.push_back(autocorr);
    
}

void extract_all_features(const vector<float>& ax_buffer,
                          const vector<float>& ay_buffer,
                          const vector<float>& az_buffer,
                          const vector<float>& gx_buffer,
                          const vector<float>& gy_buffer,
                          const vector<float>& gz_buffer,
                          int buffer_index, vector<float>& features) {

    
    compute_features(ax_buffer,
                    false, false, true,   // Mean, Std, Rms
                    true, true,          // Max , Min
                    true, true,         // Median, Zero
                    false, true,         // Ske, Kur
                    true, false, false,   // Q1,Q3, Corr
                    buffer_index, features);

    compute_features(ay_buffer,
                    false, false, true,   // Mean, Std, Rms
                    false, false,          // Max , Min
                    false, false,         // Median, Zero
                    false, false,         // Ske, Kur
                    false, false, false,   // Q1,Q3, Corr
                    buffer_index,features);

    compute_features(az_buffer,
                    false, false, false,   // Mean, Std, Rms
                    false, false,          // Max , Min
                    false, false,         // Median, Zero
                    false, false,         // Ske, Kur
                    false, false, false,   // Q1,Q3, Corr
                    buffer_index,features);

    compute_features(gx_buffer,
                    false, false, false,   // Mean, Std, Rms
                    false, false,          // Max , Min
                    false, false,         // Median, Zero
                    false, false,         // Ske, Kur
                    false, false, false,   // Q1,Q3, Corr
                    buffer_index,features);

    compute_features(gy_buffer,
                    false, false, false,   // Mean, Std, Rms
                    false, false,          // Max , Min
                    false, false,         // Median, Zero
                    false, false,         // Ske, Kur
                    false, false, false,   // Q1,Q3, Corr
                    buffer_index,features);


    compute_features(gz_buffer,
                    false, false, false,   // Mean, Std, Rms
                    false, false,          // Max , Min
                    false, false,         // Median, Zero
                    false, true,         // Ske, Kur
                    false, false, true,   // Q1,Q3, Corr
                    buffer_index,features);


}

#endif  // EXTRACT_FEATURES_H
