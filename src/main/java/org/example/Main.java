package org.example;


import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.avcodec;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

//public class Main { // Declares the main class
//    static class FilteringContext { // Inner class to hold filtering context
//        AVFilterContext bufferSinkContext; // Buffer sink filter context
//        AVFilterContext bufferSourceContext; // Buffer source filter context
//        AVFilterGraph filterGraph; // Filter graph
//    }
//
//    static Main.FilteringContext[] filteringContexts;
//
//    static class StreamContext {
//        AVCodecContext decoderContext;
//        AVCodecContext encoderContext;
//    }
//
//    static Main.StreamContext[] streamContexts;
//
//    static AVFormatContext inputFormatContext;
//    static AVFormatContext outputFormatContext;
//
//    static void check(int err) { // Method to check for errors
//        if (err < 0) { // If an error occurred
//            BytePointer e = new BytePointer(512); // Create a byte pointer of size 512
//            av_strerror(err, e, 512); // Get the error string
//            throw new RuntimeException(e.getString().substring(0, (int) BytePointer.strlen(e)) + ":" + err); // Throw a RuntimeException with the error message and error code
//        }
//    }
//
//    static void openInput(String fileName) { // Method to open an input file
//        inputFormatContext = new AVFormatContext(null); // Create a new input format context
//        check(avformat_open_input(inputFormatContext, fileName, null, null)); // Open the input file
//        check(avformat_find_stream_info(inputFormatContext, (PointerPointer) null)); // Find stream information
//        streamContexts = new Main.StreamContext[inputFormatContext.nb_streams()]; // Create an array of stream contexts for each stream
//        for (int i = 0; i < inputFormatContext.nb_streams(); i++) { // Loop through each stream
//            streamContexts[i] = new Main.StreamContext(); // Create a new stream context
//            AVStream stream = inputFormatContext.streams(i); // Get the stream
//            AVCodec decoder = avcodec_find_decoder(stream.codecpar().codec_id()); // Find the appropriate decoder for the stream
//            if (decoder == null)
//                new RuntimeException("Unexpected decoder: " + stream.codecpar().codec_id()); // Throw an exception if no decoder is found
//            AVCodecContext codecContext = avcodec_alloc_context3(decoder); // Allocate a new codec context for the decoder
//            check(avcodec_parameters_to_context(codecContext, stream.codecpar())); // Copy codec parameters from the stream to the codec context
//            if (codecContext.codec_type() == AVMEDIA_TYPE_VIDEO || codecContext.codec_type() == AVMEDIA_TYPE_AUDIO) { // If the stream is video or audio
//                if (codecContext.codec_type() == AVMEDIA_TYPE_VIDEO) { // If the stream is video
//                    codecContext.framerate(av_guess_frame_rate(inputFormatContext, stream, null)); // Guess the frame rate
//                }
//                check(avcodec_open2(codecContext, decoder, (AVDictionary) null)); // Open the decoder
//            }
//            streamContexts[i].decoderContext = codecContext; // Store the decoder context in the stream context
//        }
//        av_dump_format(inputFormatContext, 0, fileName, 0); // Dump the input format information
//    }
//
//    static AVFormatContext openOutput(String fileName) { // Method to open an output file
//        outputFormatContext = new AVFormatContext(null); // Create a new output format context
//        check(avformat_alloc_output_context2(outputFormatContext, null, null, fileName)); // Allocate the output format context with the given file name
//        for (int i = 0; i < inputFormatContext.nb_streams(); i++) { // Loop through each stream in the input
//            AVCodec c = new AVCodec(null); // Create a new codec
//            AVStream outStream = avformat_new_stream(outputFormatContext, c); // Create a new output stream
//            AVStream inStream = inputFormatContext.streams(i); // Get the input stream
//            AVCodecContext decoderContext = streamContexts[i].decoderContext; // Get the decoder context
//            if (decoderContext.codec_type() == AVMEDIA_TYPE_VIDEO || decoderContext.codec_type() == AVMEDIA_TYPE_AUDIO) { // If the stream is video or audio
//                AVCodec encoder; // Declare a variable for the encoder
//                if (decoderContext.codec_type() == AVMEDIA_TYPE_VIDEO) { // If the stream is video
//                    encoder = avcodec_find_encoder(AV_CODEC_ID_MPEG4); // Use the MPEG-4 video encoder
//                } else { // If the stream is audio
//                    encoder = avcodec_find_encoder(decoderContext.codec_id()); // Use the same codec as the input
//                }
//                AVCodecContext encoderContext = avcodec_alloc_context3(encoder); // Allocate a new encoder context
//                if (decoderContext.codec_type() == AVMEDIA_TYPE_VIDEO) { // If the stream is video
//                    encoderContext.height(decoderContext.height()); // Set the height of the encoder context
//                    encoderContext.width(decoderContext.width()); // Set the width of the encoder context
//                    encoderContext.sample_aspect_ratio(decoderContext.sample_aspect_ratio()); // Set the sample aspect ratio of the encoder context
//                    if (encoder.pix_fmts() != null && encoder.pix_fmts().asBuffer() != null) { // If the encoder has a list of supported pixel formats
//                        encoderContext.pix_fmt(encoder.pix_fmts().get(0)); // Use the first pixel format
//                    } else { // If the encoder does not have a list of supported pixel formats
//                        encoderContext.pix_fmt(decoderContext.pix_fmt()); // Use the same pixel format as the input
//                    }
//                    encoderContext.time_base(av_inv_q(decoderContext.framerate())); // Set the time base of the encoder context
//                } else { // If the stream is audio
//                    encoderContext.sample_rate(decoderContext.sample_rate()); // Set the sample rate of the encoder context
//                    encoderContext.channel_layout(decoderContext.channel_layout()); // Set the channel layout of the encoder context
//                    encoderContext.channels(av_get_channel_layout_nb_channels(encoderContext.channel_layout())); // Set the number of channels of the encoder context
//                    encoderContext.sample_fmt(encoder.sample_fmts().get(0)); // Use the first sample format supported by the encoder
//                    encoderContext.time_base(av_make_q(1, encoderContext.sample_rate())); // Set the time base of the encoder context
//                }
//
//                check(avcodec_open2(encoderContext, encoder, (AVDictionary) null)); // Open the encoder
//                check(avcodec_parameters_from_context(outStream.codecpar(), encoderContext)); // Copy the encoder parameters to the output stream
//                if ((outputFormatContext.oformat().flags() & AVFMT_GLOBALHEADER) == AVFMT_GLOBALHEADER) { // If the output format requires a global header
//                    encoderContext.flags(encoderContext.flags() | CODEC_FLAG_GLOBAL_HEADER); // Set the global header flag in the encoder context
//                }
//                outStream.time_base(encoderContext.time_base()); // Set the time base of the output stream
//                streamContexts[i].encoderContext = encoderContext; // Store the encoder context in the stream context
//            }
//        }
//        av_dump_format(outputFormatContext, 0, fileName, 1); // Dump the output format information
//
//        if ((outputFormatContext.flags() & AVFMT_NOFILE) != AVFMT_NOFILE) { // If the output format requires a file
//            AVIOContext c = new AVIOContext(); // Create a new IO context
//            check(avio_open(c, fileName, AVIO_FLAG_WRITE)); // Open the output file for writing
//            outputFormatContext.pb(c); // Set the IO context in the output format context
//        }
//
//        check(avformat_write_header(outputFormatContext, (AVDictionary) null)); // Write the header for the output format
//        return outputFormatContext; // Return the output format context
//    }
//
//    static void initFilter(FilteringContext filteringContext, AVCodecContext decoderContext,
//                           AVCodecContext encoderContext, String filterSpec) { // Method to initialize a filter
//        AVFilterInOut outputs = avfilter_inout_alloc(); // Allocate memory for output filter
//        AVFilterInOut inputs = avfilter_inout_alloc(); // Allocate memory for input filter
//        AVFilterGraph filterGraph = avfilter_graph_alloc(); // Allocate memory for the filter graph
//        AVFilter buffersink = null; // Buffer sink filter
//        AVFilter buffersrc = null; // Buffer source filter
//        AVFilterContext buffersrcContext = new AVFilterContext(); // Buffer source filter context
//        AVFilterContext buffersinkContext = new AVFilterContext(); // Buffer sink filter context
//        try {
//
//            if (decoderContext.codec_type() == AVMEDIA_TYPE_VIDEO) { // If the codec type is video
//                buffersrc = avfilter_get_by_name("buffer"); // Get the buffer source filter
//                buffersink = avfilter_get_by_name("buffersink"); // Get the buffer sink filter
//                String args = String.format("video_size=%dx%d:pix_fmt=%d:time_base=%d/%d:pixel_aspect=%d/%d",
//                        decoderContext.width(), decoderContext.height(), decoderContext.pix_fmt(),
//                        decoderContext.time_base().num(), decoderContext.time_base().den(),
//                        decoderContext.sample_aspect_ratio().num(), decoderContext.sample_aspect_ratio().den()); // Create the arguments for the buffer source filter
//                check(avfilter_graph_create_filter(buffersrcContext, buffersrc, "in", args, null, filterGraph)); // Create the buffer source filter
//                check(avfilter_graph_create_filter(buffersinkContext, buffersink, "out", null, null, filterGraph)); // Create the buffer sink filter
//                BytePointer pixFmt = new BytePointer(4).putInt(encoderContext.pix_fmt()); // Get the pixel format of the encoder context
//                check(av_opt_set_bin(buffersinkContext, "pix_fmts", pixFmt, 4, AV_OPT_SEARCH_CHILDREN)); // Set the pixel format for the buffer sink filter
//            } else {
//                if (decoderContext.codec_type() == AVMEDIA_TYPE_AUDIO) { // If the codec type is audio
//                    buffersrc = avfilter_get_by_name("abuffer"); // Get the audio buffer source filter
//                    buffersink = avfilter_get_by_name("abuffersink"); // Get the audio buffer sink filter
//                    if (decoderContext.channel_layout() == 0) { // If the channel layout is not set
//                        decoderContext.channel_layout(av_get_default_channel_layout(decoderContext.channels())); // Get the default channel layout
//                    }
//                    BytePointer name = new BytePointer(100); // Create a byte pointer to hold the channel layout name
//                    av_get_channel_layout_string(name, 100, decoderContext.channels(), decoderContext.channel_layout()); // Get the channel layout name
//                    String chLayout = name.getString().substring(0, (int) BytePointer.strlen(name)); // Get the channel layout name as a string
//                    String args = String.format("time_base=%d/%d:sample_rate=%d:sample_fmt=%s:channel_layout=%s",
//                            decoderContext.time_base().num(), decoderContext.time_base().den(), decoderContext.sample_rate(),
//                            av_get_sample_fmt_name(decoderContext.sample_fmt()).getString(), chLayout); // Create the arguments for the audio buffer source filter
//                    check(avfilter_graph_create_filter(buffersrcContext, buffersrc, "in", args, null, filterGraph)); // Create the audio buffer source filter
//                    check(avfilter_graph_create_filter(buffersinkContext, buffersink, "out", null, null, filterGraph)); // Create the audio buffer sink filter
//                    BytePointer smplFmt = new BytePointer(4).putInt(encoderContext.sample_fmt()); // Get the sample format of the encoder context
//                    check(av_opt_set_bin(buffersinkContext, "sample_fmts", smplFmt, 4, AV_OPT_SEARCH_CHILDREN)); // Set the sample format for the audio buffer sink filter
//                    BytePointer chL = new BytePointer(8).putLong(encoderContext.channel_layout()); // Get the channel layout of the encoder context
//                    check(av_opt_set_bin(buffersinkContext, "channel_layouts", chL, 8, AV_OPT_SEARCH_CHILDREN)); // Set the channel layout for the audio buffer sink filter
//                    BytePointer sr = new BytePointer(8).putLong(encoderContext.sample_rate()); // Get the sample rate of the encoder context
//                    check(av_opt_set_bin(buffersinkContext, "sample_rates", sr, 8, AV_OPT_SEARCH_CHILDREN)); // Set the sample rate for the audio buffer sink filter
//                } else { // If the codec type is neither video nor audio
//                    throw new RuntimeException(); // Throw a RuntimeException
//                }
//            }
//            outputs.name(new BytePointer(av_strdup("in"))); // Set the name of the output filter
//            outputs.filter_ctx(buffersrcContext); // Set the buffer source filter context
//            outputs.pad_idx(0); // Set the pad index
//            outputs.next(null); // Set the next output filter to null
//
//            inputs.name(new BytePointer(av_strdup("out"))); // Set the name of the input filter
//            inputs.filter_ctx(buffersinkContext); // Set the buffer sink filter context
//            inputs.pad_idx(0); // Set the pad index
//            inputs.next(null); // Set the next input filter to null
//
//            check(avfilter_graph_parse_ptr(filterGraph, filterSpec, inputs, outputs, null)); // Parse the filter graph
//            check(avfilter_graph_config(filterGraph, null)); // Configure the filter graph
//
//            filteringContext.bufferSourceContext = buffersrcContext; // Set the buffer source filter context in the filtering context
//            filteringContext.bufferSinkContext = buffersinkContext; // Set the buffer sink filter context in the filtering context
//            filteringContext.filterGraph = filterGraph; // Set the filter graph in the filtering context
//        } finally {
//            avfilter_inout_free(inputs); // Free the memory allocated for the input filter
//            avfilter_inout_free(outputs); // Free the memory allocated for the output filter
//        }
//    }
//
//    static void initFilters() { // Method to initialize filters
//        filteringContexts = new FilteringContext[inputFormatContext.nb_streams()]; // Create an array of FilteringContext objects for each input stream
//        for (int i = 0; i < inputFormatContext.nb_streams(); i++) { // Loop through each input stream
//            String filterSpec = null; // Variable to hold the filter specification
//            if (!(inputFormatContext.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_AUDIO
//                    || inputFormatContext.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_VIDEO)) { // If the stream is neither audio nor video
//                continue; // Skip this stream
//            }
//            filteringContexts[i] = new FilteringContext(); // Create a new FilteringContext object
//            if (inputFormatContext.streams(i).codecpar().codec_type() == AVMEDIA_TYPE_VIDEO) { // If the stream is video
//                filterSpec = "null"; // Set the filter specification to "null" (no filter)
//            } else { // If the stream is audio
//                filterSpec = "anull"; // Set the filter specification to "anull" (no filter)
//            }
//            initFilter(filteringContexts[i], streamContexts[i].decoderContext, streamContexts[i].encoderContext, filterSpec); // Initialize the filter for this stream
//        }
//    }
//
//
//    static boolean encodeWriteFrame(AVFrame filterFrame, int streamIndex) { // Method to encode and write a frame
//        AVPacket encodedPacket = new AVPacket(); // Create a new packet to hold the encoded frame
//        encodedPacket.data(null); // Set the packet data to null
//        encodedPacket.size(0); // Set the packet size to 0
//        av_init_packet(encodedPacket); // Initialize the packet
//        int[] gotFrameLocal = new int[1]; // Array to hold the "got frame" flag
//        if (inputFormatContext.streams(streamIndex).codecpar().codec_type() == AVMEDIA_TYPE_VIDEO) { // If the stream is video
//            check(avcodec_encode_video2(streamContexts[streamIndex].encoderContext, encodedPacket, filterFrame, gotFrameLocal)); // Encode the video frame
//        } else { // If the stream is audio
//            check(avcodec_encode_audio2(streamContexts[streamIndex].encoderContext, encodedPacket, filterFrame, gotFrameLocal)); // Encode the audio frame
//        }
//        av_frame_free(filterFrame); // Free the memory used by the frame
//        if (gotFrameLocal[0] == 0) { // If no frame was encoded
//            return false; // Return false
//        }
//        encodedPacket.stream_index(streamIndex); // Set the stream index in the packet
//        av_packet_rescale_ts(encodedPacket, streamContexts[streamIndex].encoderContext.time_base(),
//                outputFormatContext.streams(streamIndex).time_base()); // Rescale the packet's timestamps
//        check(av_interleaved_write_frame(outputFormatContext, encodedPacket)); // Write the encoded frame to the output
//        return true; // Return true
//    }
//
//    static void filterEncodeWriteFrame(AVFrame frame, int streamIndex) { // Method to filter, encode, and write a frame
//        check(av_buffersrc_add_frame_flags(filteringContexts[streamIndex].bufferSourceContext,
//                frame, 0)); // Add the frame to the buffer source
//        while (true) { // Loop until all filtered frames are processed
//            AVFrame filterFrame = av_frame_alloc(); // Allocate a new frame to hold the filtered frame
//            int ret = av_buffersink_get_frame(filteringContexts[streamIndex].bufferSinkContext, filterFrame); // Get the next filtered frame
//            if (ret == AVERROR_EOF() || ret == AVERROR_EAGAIN()) { // If the end of the stream or no frame is available
//                av_frame_free(filterFrame); // Free the memory used by the frame
//                return; // Exit the method
//            }
//            check(ret); // Check for other errors
//            filterFrame.pict_type(AV_PICTURE_TYPE_NONE); // Set the picture type to none
//            encodeWriteFrame(filterFrame, streamIndex); // Encode and write the filtered frame
//        }
//    }
//
//    static void flushEncoder(int streamIndex) { // Method to flush the encoder
//        if ((streamContexts[streamIndex].encoderContext.codec().capabilities() & AV_CODEC_CAP_DELAY)
//                != AV_CODEC_CAP_DELAY) { // If the encoder does not have a delay
//            return; // Exit the method
//        }
//        while (encodeWriteFrame(null, streamIndex))
//            ; // Loop and call encodeWriteFrame with null frame until it returns false
//    }
//
//    public static void main(String[] args) throws IOException {
//
////        if (args.length < 2) {
////            System.err.println("Usage: program <input_file> <output_file>");
////            System.exit(1);
////        }
//
//        av_register_all();
//        avfilter_register_all();
//
//        openInput("C:\\Users\\amirkhb\\IdeaProjects\\antMediaCoding\\src\\main\\java\\org\\example\\input.flv");
//        openOutput("C:\\Users\\amirkhb\\IdeaProjects\\antMediaCoding\\src\\main\\java\\org\\example\\output.mp4");
////        String inputFile = args[0];
////        String outputFile = args[1];
////
////        openInput(inputFile);
////        openOutput(outputFile);
//
//        initFilters();
//        try {
//            int[] gotFrame = new int[1]; // Array to hold the "got frame" flag
//            AVPacket packet = new AVPacket(); // Create a packet to read frames
//            while (av_read_frame(inputFormatContext, packet) >= 0) { // Loop through each packet in the input file
//                try {
//                    int streamIndex = packet.stream_index(); // Get the stream index of the packet
//                    int type = inputFormatContext.streams(streamIndex).codecpar().codec_type(); // Get the codec type of the stream
//                    if (filteringContexts[streamIndex].filterGraph != null) { // If there is a filter graph for this stream
//                        AVFrame frame = av_frame_alloc(); // Allocate a frame to hold the decoded frame
//                        try {
//
//                            av_packet_rescale_ts(packet, inputFormatContext.streams(streamIndex).time_base(),
//                                    streamContexts[streamIndex].decoderContext.time_base()); // Rescale the packet's timestamps
//                            if (type == AVMEDIA_TYPE_VIDEO) { // If the stream is video
//                                check(avcodec_decode_video2(streamContexts[streamIndex].decoderContext, frame, gotFrame, packet)); // Decode the video packet
//                            } else { // If the stream is audio
//                                check(avcodec_decode_audio4(streamContexts[streamIndex].decoderContext, frame, gotFrame, packet)); // Decode the audio packet
//                            }
//                            if (gotFrame[0] != 0) { // If a frame was decoded
//                                frame.pts(frame.best_effort_timestamp()); // Set the presentation timestamp of the frame
//                                filterEncodeWriteFrame(frame, streamIndex); // Filter, encode, and write the frame
//                            }
//                        } finally {
//                            av_frame_free(frame); // Free the memory used by the frame
//                        }
//                    } else { // If there is no filter graph for this stream
//                        av_packet_rescale_ts(packet, inputFormatContext.streams(streamIndex).time_base(),
//                                outputFormatContext.streams(streamIndex).time_base()); // Rescale the packet's timestamps
//                        check(av_interleaved_write_frame(outputFormatContext, packet)); // Write the packet to the output
//                    }
//                } finally {
//                    av_packet_unref(packet); // Unreference the packet
//                }
//            }
//
//            for (int i = 0; i < inputFormatContext.nb_streams(); i++) { // Loop through each stream
//                if (filteringContexts[i].filterGraph == null) { // If there is no filter graph for this stream
//                    continue; // Skip this stream
//                }
//                filterEncodeWriteFrame(null, i); // Flush the filter by passing a null frame
//                flushEncoder(i); // Flush the encoder for this stream
//            }
//
//            av_write_trailer(outputFormatContext); // Write the trailer for the output file
//
//        } finally {
//            for (int i = 0; i < inputFormatContext.nb_streams(); i++) { // Loop through each stream
//                avcodec_free_context(streamContexts[i].decoderContext); // Free the decoder context
//                if (outputFormatContext != null && outputFormatContext.nb_streams() > 0 &&
//                        outputFormatContext.streams(i) != null && streamContexts[i].encoderContext != null) { // If there is an encoder context
//                    avcodec_free_context(streamContexts[i].encoderContext); // Free the encoder context
//                }
//                if (filteringContexts != null && filteringContexts[i].filterGraph != null) { // If there is a filter graph
//                    avfilter_graph_free(filteringContexts[i].filterGraph); // Free the filter graph
//                }
//            }
//            avformat_close_input(inputFormatContext); // Close the input file
//            if (outputFormatContext != null && (outputFormatContext.oformat().flags() & AVFMT_NOFILE) != AVFMT_NOFILE) { // If the output format requires a file
//                avio_closep(outputFormatContext.pb()); // Close the output file
//            }
//            avformat_free_context(outputFormatContext); // Free the output format context
//        }
//    }
//}

public class Main {
    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.println("usage: input output");
//            return;
//        }
        String inputFilename = "C:\\Users\\amirkhb\\IdeaProjects\\antMediaCoding\\src\\main\\java\\org\\example\\input.flv";
        String outputFilename = "C:\\Users\\amirkhb\\IdeaProjects\\antMediaCoding\\src\\main\\java\\org\\example\\output.mp4";

        av_register_all();

        AVFormatContext ifmt_ctx = new AVFormatContext(null);
        if (avformat_open_input(ifmt_ctx, inputFilename, null, null) < 0) {
            System.err.println("Could not open input file '" + inputFilename + "'");
            return;
        }

        if (avformat_find_stream_info(ifmt_ctx, (PointerPointer<?>) null) < 0) {
            System.err.println("Failed to retrieve input stream information");
            return;
        }

        av_dump_format(ifmt_ctx, 0, inputFilename, 0);

        AVFormatContext ofmt_ctx = new AVFormatContext(null);
        avformat_alloc_output_context2(ofmt_ctx, null, null, outputFilename);
        if (ofmt_ctx.isNull()) {
            System.err.println("Could not create output context");
            return;
        }

        AVOutputFormat ofmt = ofmt_ctx.oformat();

        for (int i = 0; i < ifmt_ctx.nb_streams(); i++) {
            AVStream in_stream = ifmt_ctx.streams(i);
            AVStream out_stream = avformat_new_stream(ofmt_ctx, null);
            if (out_stream.isNull()) {
                System.err.println("Failed allocating output stream");
                return;
            }
            if (avcodec_copy_context(out_stream.codec(), in_stream.codec()) < 0) {
                System.err.println("Failed to copy context from input to output stream codec context");
                return;
            }
            if ((ofmt.flags() & AVFMT_GLOBALHEADER) != 0)
                out_stream.codec().flags(avcodec.AV_CODEC_FLAG_GLOBAL_HEADER);
        }

        av_dump_format(ofmt_ctx, 0, outputFilename, 1);


        AVIOContext pb = new AVIOContext(null);
        avio_open(pb, outputFilename, AVIO_FLAG_WRITE);
        ofmt_ctx.pb(pb);

//        if ((ofmt.flags() & AVFMT_NOFILE) == 0) {
//            if (avio_open(pb, outputFilename, AVIO_FLAG_WRITE) < 0) {
//                System.err.println("Could not open output file '" + outputFilename + "'");
//                return;
//            }
//        }


        if (avformat_write_header(ofmt_ctx, (PointerPointer) null) < 0) {
            System.err.println("Error occurred when opening output file");
            return;
        }

        AVPacket pkt = new AVPacket();
        while (true) {
            int ret = av_read_frame(ifmt_ctx, pkt);
            if (ret < 0)
                break;
            AVStream in_stream = ifmt_ctx.streams(pkt.stream_index());
            AVStream out_stream = ofmt_ctx.streams(pkt.stream_index());
            log_packet(ifmt_ctx, pkt, "in");
            pkt.pts(av_rescale_q_rnd(pkt.pts(), in_stream.time_base(), out_stream.time_base(), AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
            pkt.dts(av_rescale_q_rnd(pkt.dts(), in_stream.time_base(), out_stream.time_base(), AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
            pkt.duration(av_rescale_q(pkt.duration(), in_stream.time_base(), out_stream.time_base()));
            pkt.pos(-1);
            log_packet(ofmt_ctx, pkt, "out");
            if (av_interleaved_write_frame(ofmt_ctx, pkt) < 0) {
                System.err.println("Error muxing packet");
                break;
            }
            av_packet_unref(pkt);
        }

        av_write_trailer(ofmt_ctx);

        avformat_close_input(ifmt_ctx);

        if ((ofmt.flags() & AVFMT_NOFILE) == 0) {
            avio_close(ofmt_ctx.pb());
        }
        avformat_free_context(ofmt_ctx);
    }

    static void log_packet(AVFormatContext fmt_ctx, AVPacket pkt, String tag) {
        AVRational time_base = fmt_ctx.streams(pkt.stream_index()).time_base();
        System.out.printf("%s: pts:%s pts_time:%s dts:%s dts_time:%s duration:%s duration_time:%s stream_index:%d\n",
                tag,
                pkt.pts(), ts2timestr(pkt.pts(), time_base),
                pkt.dts(), ts2timestr(pkt.dts(), time_base),
                pkt.duration(), ts2timestr(pkt.duration(), time_base),
                pkt.stream_index());
    }

    static String ts2timestr(long ts, AVRational time_base) {
        double timeSeconds = ts * av_q2d(time_base);
        int hours = (int) (timeSeconds / 3600);
        int minutes = (int) ((timeSeconds % 3600) / 60);
        int seconds = (int) (timeSeconds % 60);
        int ms = (int) (timeSeconds * 1000) % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }
}