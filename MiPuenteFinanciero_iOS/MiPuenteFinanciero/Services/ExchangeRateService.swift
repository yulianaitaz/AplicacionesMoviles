import Foundation

struct ExchangeRateService {
    private struct ExchangeResponse: Decodable {
        let result: String
        let rates: [String: Double]
    }

    func fetchUSDToCOP() async throws -> Double {
        let url = URL(string: "https://open.er-api.com/v6/latest/USD")!
        var request = URLRequest(url: url)
        request.timeoutInterval = 10
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw ExchangeError.invalidResponse
        }
        let payload = try JSONDecoder().decode(ExchangeResponse.self, from: data)
        guard payload.result == "success", let cop = payload.rates["COP"], cop > 0 else {
            throw ExchangeError.invalidResponse
        }
        return cop
    }
}

enum ExchangeError: LocalizedError {
    case invalidResponse
    var errorDescription: String? { "El servicio de tasa de cambio no entregó un dato válido." }
}
