const path = require('path');
const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
  entry: './src/index.ts',
  devtool: 'inline-source-map',
  mode: 'development',
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      {
          test: /\.css$/,
          use: [
            'style-loader',
            'css-loader'
          ]
        }
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  output: {
    filename: '[name].bundle.js',
    path: path.resolve(__dirname, 'dist'),
    clean: true,
  },

  devServer: {
    static: path.join(__dirname, 'dist'),
    port: 8000,
     headers: {
      'Cache-Control': 'no-store',
    },
    proxy: {
      '/api': {
         target: {
            host: "localhost",
            protocol: 'http:',
            port: 8080
         },
      }
    }
  },
  optimization: {
    runtimeChunk: 'single',
  },

  plugins: [
    new CopyPlugin({
      patterns: [
        { from: "src/static/*", to: "./[name][ext]"},
      ],
    }),
  ],
};